package org.xxpay.pay.channel.anyipay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
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
public class AnyipayPayNotifyService extends BasePayNotify {
    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ANYI;
    }

    private static final MyLog _log = MyLog.getLog(AnyipayPaymentService.class);

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理安逸支付回调】";
        _log.info("====== 开始处理安逸支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("pid", request.getParameter("pid"));
            params.put("trade_no", request.getParameter("trade_no"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("type", request.getParameter("type"));
            params.put("name", request.getParameter("name"));
            params.put("money", request.getParameter("money"));
            params.put("trade_status", request.getParameter("trade_status"));
            params.put("sign_type", request.getParameter("sign_type"));
            params.put("sign", request.getParameter("sign"));
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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理安逸支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证安逸支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String merchant = params.getString("pid");  //商户号
        String amount = params.getString("money"); // 订单金额
        BigDecimal b = new BigDecimal(params.getString("money"));
        amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String sys_order_no = params.getString("trade_no"); // 平台流水号
        String outOrderNo = params.getString("out_trade_no"); // 商户订单号
        String type = params.getString("type"); //支付类型
        String name = params.getString("name"); //商品名称

        String sign = params.getString("sign");  //签名
        String singType = params.getString("sign_type");// 签名类型
        String tradeStatus = params.getString("trade_status"); //支付状态
        // 查询payOrder记录
        String payOrderId = outOrderNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("pid", merchant);
        signMap.put("money", amount);
        signMap.put("trade_no", sys_order_no);
        signMap.put("out_trade_no", outOrderNo);
        signMap.put("type", type);
        signMap.put("name", name);
        signMap.put("trade_status", tradeStatus);

        // md5 加密
        String signStr= XXPayUtil.mapToString(signMap);
        String md5 = PayDigestUtil.md5(signStr+ channelPayConfig.getmD5Key(),"UTF-8");
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }


}
