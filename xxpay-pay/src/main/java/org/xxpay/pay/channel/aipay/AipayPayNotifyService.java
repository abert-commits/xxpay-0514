package org.xxpay.pay.channel.aipay;

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
import java.util.*;

@Service
public class AipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(AipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_AIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理艾支付回调】";
        _log.info("====== 开始处理艾支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;

        try {
            JSONObject params = new JSONObject();
            params.put("partner_id", request.getParameter("partner_id"));
            params.put("channel_id", request.getParameter("channel_id"));
            params.put("partner_order", request.getParameter("partner_order"));
            params.put("order_id", request.getParameter("order_id"));
            params.put("user_id", request.getParameter("user_id"));
            params.put("total_fee", request.getParameter("total_fee"));
            params.put("real_money", request.getParameter("real_money"));
            params.put("success_time", request.getParameter("success_time"));
            params.put("code", request.getParameter("code"));
            params.put("sign", request.getParameter("sign"));
            _log.info("支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if ("1001".equals(params.get("code"))) {
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

            if (updatePayOrderRows==1)
            {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理艾支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
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
        String partner_id = params.getString("partner_id");  //商户编号
        String channel_id	 = params.getString("channel_id"); // 渠道编号
        String partner_order = params.getString("partner_order"); // 商户订单号
        String order_id = params.getString("order_id");   //支付订单号
        String user_id = params.getString("user_id"); //用户id
        String total_fee = params.getString("total_fee"); //订单金额
        String real_money = params.getString("real_money"); //实收金额
        String success_time = params.getString("success_time"); //成功时间
        String code = params.getString("code"); //状态码
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(partner_order);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", partner_order);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        Map<String, Object> signMap = new LinkedHashMap<String, Object>();
        signMap.put("partner_id", partner_id);
        signMap.put("channel_id", channel_id);
        signMap.put("partner_order", partner_order);
        signMap.put("order_id", order_id);
        signMap.put("user_id", user_id);
        signMap.put("total_fee", total_fee);
        signMap.put("success_time", success_time);
        signMap.put("code", code);

        // md5 加密
        String md5 = PayDigestUtil.getSignLinkedHash(signMap, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", partner_order);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(total_fee)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total_fee, partner_order);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
