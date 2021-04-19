package org.xxpay.pay.channel.xinbao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class XinbaoPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(XinbaoPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【鑫宝支付回调】";
        _log.info("====== 开始处理鑫宝支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        StringBuffer jb = new StringBuffer();
        try {
            String line = null;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
            reader.close();

            SortedMap sortedMap = JSON.parseObject(jb.toString(), new TypeReference<TreeMap<String, String>>() {
            });
            JSONObject params = new JSONObject();
            params.put("SignMethod", sortedMap.get("SignMethod"));
            params.put("Signature", sortedMap.get("Signature"));
            params.put("MerNo", sortedMap.get("MerNo"));
            params.put("TxSN", sortedMap.get("TxSN"));
            params.put("Amount", sortedMap.get("Amount"));
            params.put("PaidAmount", sortedMap.get("PaidAmount"));
            params.put("PdtName", sortedMap.get("PdtName"));
            params.put("PlatTxMsg", sortedMap.get("PlatTxMsg"));
            params.put("Status", sortedMap.get("Status"));
            params.put("PlatTxSN", sortedMap.get("PlatTxSN"));
            params.put("PayFee", sortedMap.get("PayFee"));
            params.put("BankTxSN", sortedMap.get("BankTxSN"));
            params.put("TxTime", sortedMap.get("TxTime"));

            _log.info("鑫宝支付回调=============" + params.toJSONString());
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }

            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 1-支付成功
            if ("1".equals(params.get("Status"))) {
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
            _log.info("====== 完成处理鑫宝支付回调通知 ======");
            respString = "success";
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
        String resign = params.getString("Signature");                //签名
        String amount = params.getString("Amount");                    // 订单金额
        // 查询payOrder记录
        String payOrderId = params.getString("TxSN");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        params.remove("Signature");
        params.remove("SignMethod");
        SortedMap<String, String> myMap = XXPayUtil.JSONObjectToSortedMap(params);
        String signStr = XXPayUtil.mapToString(myMap).replace(">", "");
        String signValue = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "utf-8");
        if (!resign.toUpperCase().equals(signValue.toUpperCase())) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(amount).longValue();//乘以100
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }


        payContext.put("payOrder", payOrder);
        return true;
    }
}