package org.xxpay.pay.channel.xiaozuanfeng;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
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
public class XiaozuanfengPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(XiaozuanfengPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SHUNFENG;
    }


    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理顺丰支付回调】";
        _log.info("====== 开始处理顺丰支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("merchant_no", request.getParameter("merchant_no"));
            params.put("merchant_order_sn", request.getParameter("merchant_order_sn"));
            params.put("order_sn", request.getParameter("order_sn"));
            params.put("pay_status", request.getParameter("pay_status"));
            params.put("trade_amount", request.getParameter("trade_amount"));
            params.put("rand_str", request.getParameter("rand_str"));
            params.put("pay_time", request.getParameter("pay_time"));
            params.put("sign", request.getParameter("sign"));
            params.put("paychannel_type", request.getParameter("paychannel_type"));
            _log.info("支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            if (!CheckCallIP(request, payOrder.getPassageId(), payOrder)) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return  retObj;
            }
            int updatePayOrderRows = 0;
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if ("1".equals(params.get("pay_status"))) {
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
            _log.info("====== 完成处理顺丰支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
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
        String merchant_no = params.getString("merchant_no");
        String merchant_order_sn = params.getString("merchant_order_sn");
        String order_sn = params.getString("order_sn");
        String pay_status = params.getString("pay_status");
        String trade_amount = params.getString("trade_amount");
        String rand_str = params.getString("rand_str");
        String pay_time = params.getString("pay_time");
        String paychannel_type = params.getString("paychannel_type");

        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(merchant_order_sn);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", merchant_order_sn);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("merchant_no", merchant_no);
        signMap.put("merchant_order_sn", merchant_order_sn);
        signMap.put("order_sn", order_sn);
        signMap.put("pay_status", pay_status);
        signMap.put("trade_amount", trade_amount);
        signMap.put("rand_str", rand_str);
        signMap.put("pay_time", pay_time);
        signMap.put("paychannel_type", paychannel_type);

        String sendM = XXPayUtil.mapToString(signMap);
        boolean f = RSASHA256withRSAUtils.verify(sendM.getBytes("UTF-8"), channelPayConfig.getRsaPublicKey(),sign,"SHA1withRSA");
        _log.info("******************sign:{}", sign);
        if (!f) {
            _log.error("验证签名失败. payOrderId={}, ", merchant_order_sn);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(trade_amount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", trade_amount, merchant_order_sn);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
