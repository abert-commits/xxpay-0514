package org.xxpay.pay.channel.yili;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;
import org.xxpay.pay.channel.sandpay.sdk.AESTool;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class YilipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(YilipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理一粒支付回调】";
        _log.info("====== 开始处理一粒支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("linkId", request.getParameter("linkId"));
            params.put("orderNo", request.getParameter("orderNo"));
            params.put("orderStatus", request.getParameter("orderStatus"));
            params.put("orderTime", request.getParameter("orderTime"));
            params.put("orderMemo", request.getParameter("orderMemo"));
            params.put("orderAmount", request.getParameter("orderAmount"));
            params.put("orderReal", request.getParameter("orderReal"));
            params.put("sign", request.getParameter("sign"));
            _log.info("一粒支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
            if ("20".equals(params.get("orderStatus"))) {
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

            _log.info("====== 完成处理一粒支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证JQK支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String linkId = params.getString("linkId");   //
        String orderNo = params.getString("orderNo");   //
        String orderStatus = params.getString("orderStatus");   //

        String orderAmount = params.getString("orderAmount");   //
        String resign = params.getString("sign");                //签名

        // 查询payOrder记录
        String payOrderId = linkId;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        StringBuilder signBuffer = new StringBuilder();
        signBuffer.append(orderNo);
        signBuffer.append(orderStatus);
        signBuffer.append(channelPayConfig.getmD5Key());
        String sign = PayDigestUtil.md5(signBuffer.toString(), "UTF-8");

        if (!resign.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(orderAmount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", orderAmount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }


        payContext.put("payOrder", payOrder);
        return true;
    }
}
