package org.xxpay.pay.channel.aoligei;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.baixiong.AESUtil;
import org.xxpay.pay.channel.baixiong.SignUtils;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AoligeiPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(AoligeiPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理奥利给支付回调】";
        _log.info("====== 开始处理奥利给支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            String content = request.getParameter("content");
            params.put("content", content);
            params.put("mno", request.getParameter("mno"));
            params.put("orderno", request.getParameter("orderno"));
            _log.info("晴天奥利给回调报文=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");

            payOrder = (PayOrder) payContext.get("payOrder");
            if (!CheckCallIP(request, payOrder.getPassageId(), payOrder)) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return  retObj;
            }

            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 2-支付成功
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
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理奥利给支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证晴天神码通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String content = params.getString("content");
        String mno = params.getString("mno");
        String orderno = params.getString("orderno");
        // 查询payOrder记录
        String payOrderId =orderno;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        if (StringUtils.isNotEmpty(content)) {
            String decodeContent = AESUtil.decryptBase64(content, channelPayConfig.getmD5Key());
            if (checkSign(decodeContent,channelPayConfig.getRsaprivateKey(),payOrder.getAmount())){
                payContext.put("payOrder", payOrder);
                return true;
            }
        }
        _log.error("验证签名失败. payOrderId={}, ", payOrderId);
        payContext.put("retMsg", "验证签名失败");
        return false;
        /*SortedMap sortedMap= XXPayUtil.JSONObjectToSortedMap(params);
        sortedMap.remove("sign");
        String signStr = MapConvertStr(sortedMap);
        signStr = channelPayConfig.getmD5Key() + signStr + channelPayConfig.getmD5Key();
        String sign = PayDigestUtil.md5(signStr, "UTF-8");
        if (!resign.equals(sign.toUpperCase())) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }*/
/*
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }*/

    }
    //校验
    private static boolean checkSign(String decodeContent,String pass,long amount){
        JSONObject json = JSONObject.parseObject(decodeContent);
        String status = json.getString("status");
        long ramount = json.getLong("amount");

        String excludeSign =  json.remove("sign").toString();
        Map<String,String> params = new HashMap<>();
        for (Map.Entry<String,Object> entry : json.entrySet()){
            params.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        String sign = SignUtils.getSign(params,pass);
        return excludeSign.equals(sign) && "1".equals(status)&&ramount==amount;
    }
}
