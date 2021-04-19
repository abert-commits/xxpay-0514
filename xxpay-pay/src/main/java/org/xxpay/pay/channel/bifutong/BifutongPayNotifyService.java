package org.xxpay.pay.channel.bifutong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class BifutongPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(BifutongPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理币付通支付回调】";
        _log.info("====== 开始处理币付通支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("timestamp", request.getParameter("timestamp"));
            params.put("nonce", request.getParameter("nonce"));
            params.put("sign", request.getParameter("sign"));
            params.put("body", request.getParameter("body"));
            _log.info("币付通支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
//            if ("0".equals(params.get("opstate"))) {
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
//            }
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理币付通支付回调通知 ======");
            respString = "200";
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证币付通支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String resign = params.getString("sign");//签名
        JSONObject bodyObj = JSONObject.parseObject(params.getString("body"));
        String amount = bodyObj.getString("originalLegalAmount");// 订单金额


        // 查询payOrder记录
        String payOrderId = bodyObj.getString("businessId");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        SortedMap signData = XXPayUtil.JSONObjectToSortedMap(bodyObj);
//        JSONObject signData = new JSONObject(50, true);
        String txtId = bodyObj.get("txId").toString();
        String tradeId = bodyObj.get("tradeId").toString();
        String businessId = bodyObj.get("businessId").toString();
        String originalAmount = bodyObj.get("originalAmount").toString();
        String originalLegalAmount = bodyObj.get("originalLegalAmount").toString();
        String tradeType = bodyObj.get("tradeType").toString();

//        signData.put("txId", txtId);
//        signData.put("tradeId", tradeId);
//        signData.put("businessId", businessId);
//        signData.put("originalAmount", (originalAmount));
//        signData.put("originalLegalAmount", (originalLegalAmount));
//        signData.put("tradeType", (tradeType));
        String sign = PayDigestUtil.md5(JSON.toJSONString(signData) + channelPayConfig.getRsaPublicKey() + params.getString("nonce") + params.getString("timestamp"), "UTF-8");
        if (!sign.equals(resign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }

        payContext.put("payOrder", payOrder);
        return true;
    }
}
