package org.xxpay.pay.channel.xiuer;

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

@Service
public class XiuerPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(XiuerPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_AAA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理秀儿付支付回调】";
        _log.info("====== 开始处理秀儿付支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("code", request.getParameter("code"));
            params.put("transactionid", request.getParameter("transactionid"));
            params.put("systemorderid", request.getParameter("systemorderid"));
            params.put("orderno", request.getParameter("orderno"));
            params.put("bankaccount", request.getParameter("bankaccount"));
            params.put("merno", request.getParameter("merno"));
            params.put("transamt", request.getParameter("transamt"));


            params.put("timeend", request.getParameter("timeend"));
            params.put("banktype", request.getParameter("banktype"));
            params.put("productid", request.getParameter("productid"));


            params.put("signature", request.getParameter("signature"));
            _log.info("秀儿付支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
            if ("11".equals(params.get("code"))) {
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

            _log.info("====== 完成处理秀儿付支付回调通知 ======");
            respString = "success";
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证秀儿支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String signature = params.getString("signature");                //签名
        String transamt = params.getString("transamt");                    // 订单金额

        // 查询payOrder记录
        String payOrderId = params.getString("orderno");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        params.remove("signature");
        String signValue = PayDigestUtil.getSign(params, channelPayConfig.getmD5Key()).toUpperCase();
        if (!signature.toUpperCase().equals(signValue)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }


        if (Long.valueOf(transamt) != payOrder.getAmount().longValue()) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", transamt, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }


        payContext.put("payOrder", payOrder);
        return true;
    }
}
