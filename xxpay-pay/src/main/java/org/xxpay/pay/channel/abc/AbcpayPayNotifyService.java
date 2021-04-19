package org.xxpay.pay.channel.abc;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
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
public class AbcpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(AbcpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ABC;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理ABC支付回调】";
        _log.info("====== 开始处理ABC支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("mch_id", request.getParameter("mch_id"));
            params.put("orderid", request.getParameter("orderid"));
            params.put("transaction_id", request.getParameter("transaction_id"));
            params.put("money", request.getParameter("money"));
            params.put("amount", request.getParameter("amount"));
            params.put("status", request.getParameter("status"));
            params.put("sign", request.getParameter("sign"));
            params.put("attach", request.getParameter("attach"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if (params.getInteger("status")>0) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    int updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                    if (updatePayOrderRows != 1) {
                        _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                        retObj.put(PayConstant.RESPONSE_RESULT, "处理订单失败");
                        return retObj;
                    }
                    _log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                }
            }
            // 业务系统后端通知
            baseNotify4MchPay.doNotify(payOrder, true);
            _log.info("====== 完成处理ABC支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK_1;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证环亚支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String mch_id	 = params.getString("mch_id");  //商户号
        String orderid = params.getString("orderid"); // 商户订单号
        String transaction_id = params.getString("transaction_id"); //系统流水号
        String money = params.getString("money");   //商户交易金额
        String amount = params.getString("amount"); //订单金额	以该金额为准
        String status = params.getString("status"); //交易状态 0失败 大于0成功
        String attach = params.getString("attach"); //备注

        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(orderid);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", orderid);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, Object> signMap = new TreeMap<String, Object>();
        signMap.put("mch_id", mch_id);
        signMap.put("orderid", orderid);
        signMap.put("transaction_id", transaction_id);
        signMap.put("money", money);
        signMap.put("amount", amount);
        signMap.put("status", status);
      //  signMap.put("attach", attach);
        // md5 加密
        String md5 = PayDigestUtil.getSignNotKey(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", orderid);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();//乘以100

        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, orderid);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
