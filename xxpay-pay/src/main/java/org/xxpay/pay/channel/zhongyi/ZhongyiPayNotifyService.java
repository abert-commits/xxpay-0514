package org.xxpay.pay.channel.zhongyi;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.RSASHA256withRSAUtils;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ZhongyiPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(ZhongyiPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YILIANBAO;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理中益回调】";
        _log.info("====== 开始处理中益回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        try {
            JSONObject params = new JSONObject();
            params.put("OrderId", request.getParameter("OrderId"));
            params.put("TransactionId", request.getParameter("TransactionId"));
            params.put("Amount", request.getParameter("Amount"));
            params.put("Date", request.getParameter("Date"));
            params.put("Status", request.getParameter("Status"));
            params.put("MemberId", request.getParameter("MemberId"));

            params.put("Sign", request.getParameter("Sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            int updatePayOrderRows = 0;
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
            if ("CODE_SUCCESS".equals(params.getString("Status"))) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                    if (updatePayOrderRows != 1) {
                        _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                        retObj.put(PayConstant.RESPONSE_RESULT, "处理订单失败");
                        return retObj;
                    }
                    _log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                }
            }
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }
            _log.info("====== 完成处理中益回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK_1;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证中益通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String MemberId = params.getString("MemberId");  //商户号
        String OrderId = params.getString("OrderId"); //商户订单号
        String TransactionId = params.getString("TransactionId"); //平台支付流水号
        String Amount = params.getString("Amount"); // 支付金额
        String Date = params.getString("Date"); //支付成功时间
        String Status = params.getString("Status"); //支付状态
        String Sign = params.getString("Sign");  //签名
        // 查询payOrder记录
        String payOrderId = OrderId;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("MemberId", MemberId);
        signMap.put("OrderId", OrderId);
        signMap.put("TransactionId", TransactionId);
        signMap.put("Amount", Amount);
        signMap.put("Date", Date);
        signMap.put("Status", Status);

        String sign1 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key()).toUpperCase();
        if (!sign1.equals(Sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(Amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", Amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
