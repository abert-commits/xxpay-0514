package org.xxpay.pay.channel.Junpay;

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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class JunpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(JunpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUNZHIFU;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理俊支支付回调】";
        _log.info("====== 开始处理俊支支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("userId", jsonParam.getString("userId"));
            params.put("status", jsonParam.getString("status"));
            params.put("message", jsonParam.getString("message"));
            params.put("amount", jsonParam.getString("amount"));
            params.put("payAmount", jsonParam.getString("payAmount"));
            params.put("requestNo", jsonParam.getString("requestNo"));
            params.put("orderNo", jsonParam.getString("orderNo"));
            params.put("payTime", jsonParam.getString("payTime"));
            params.put("attachData", jsonParam.getString("attachData"));


            params.put("sign", jsonParam.getString("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            int updatePayOrderRows = 0;
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if ("2".equals(params.get("status")) || "3".equals(params.get("status"))) {
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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }
            _log.info("====== 完成处理俊支支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证俊支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String mchId = params.getString("userId"); // 商户ID
        String requestNo = params.getString("requestNo"); //商户订单号
        String orderNo = params.getString("orderNo");  //系统流水号
        String amount = params.getString("amount"); //支付金额
        String payAmount = params.getString("payAmount"); //入账金额
        String status = params.getString("status"); //状态
        String payTime = params.getString("payTime"); //支付时间
        String message = params.getString("message"); //描述

        String attachData = params.getString("attachData");//附加数据
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(requestNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", requestNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("userId", mchId);
        signMap.put("requestNo", requestNo);
        signMap.put("orderNo", orderNo);
        signMap.put("amount", amount);
        signMap.put("payAmount", payAmount);
        signMap.put("status", status);
        signMap.put("payTime", payTime);
        signMap.put("message", message);

        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", requestNo);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(payAmount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, requestNo);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
