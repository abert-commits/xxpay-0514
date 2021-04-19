package org.xxpay.pay.channel.CC; //


import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
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
public class CcPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(CcPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_CC;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理CC支付回调】";
        _log.info("====== 开始处理CC支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("appID", request.getParameter("appID"));
            params.put("channel", request.getParameter("channel"));
            params.put("payTime", request.getParameter("payTime"));
            params.put("partnerNo", request.getParameter("partnerNo"));
            params.put("orderNo", request.getParameter("orderNo"));
            params.put("code", request.getParameter("code"));
            params.put("msg", request.getParameter("msg"));
            params.put("amount", request.getParameter("amount"));
            params.put("fee", request.getParameter("fee"));
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
            if ("1".equals(params.get("code"))) {
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

            _log.info("====== 完成处理CC支付回调通知 ======");
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
        String payOrderId = params.getString("orderNo");  //支付订单号
        String appId = params.getString("appID"); // 应用ID
        String productId = params.getString("channel");   //支付产品ID
        String mchOrderNo = params.getString("partnerNo"); //商户订单号
        String amount = params.getString("amount"); //支付金额
        String code = params.getString("code"); //返回代码,0支付失败，1支付成功
        String msg = params.getString("msg"); //状态
        String fee = params.getString("fee"); //手续费，本次订单产生的手续费（已经在平台账户中扣除）
        String payTime = params.getString("payTime"); //手续费，本次订单产生的手续费（已经在平台账户中扣除）
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(mchOrderNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", mchOrderNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap signMap = new TreeMap();
        signMap.put("orderNo", payOrderId);
        signMap.put("payTime", payTime);
        signMap.put("appID", appId);
        signMap.put("channel", productId);
        signMap.put("partnerNo", mchOrderNo);
        signMap.put("amount", amount);
        signMap.put("code", code);
        signMap.put("msg", msg);
        signMap.put("fee", fee);
        // md5 加密
        String md5 = PayDigestUtil.getSignNotKey(signMap, "&"+channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        String dbPayAmt = AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()));
        if (!dbPayAmt.equals(amount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}


