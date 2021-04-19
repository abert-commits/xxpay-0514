package org.xxpay.pay.channel.qiaoqiao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.baixiong.SignUtils;

import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QiaoqiaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QiaoqiaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QIAOQIAO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "TC-11");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }



    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【巧巧支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {

            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map<String,String> dataParams = new HashMap<>(); // data加密内容
            dataParams.put("money", String.valueOf(payOrder.getAmount())); // 交易金额，单位分
            dataParams.put("pay_type", channelPayConfig.getChannelId()); //
            dataParams.put("trade_type", "T1");
            String dataString = JSONObject.toJSONString(dataParams);
            String data = AESUtil.AESEncrypt(dataString, channelPayConfig.getmD5Key());

            SortedMap params = new TreeMap();
            params.put("mer_no", channelPayConfig.getMchId());
            params.put("mer_user_no", "001313001325000000000001");
            params.put("trans_id", payOrder.getPayOrderId());
            params.put("pay_time", String.valueOf(System.currentTimeMillis()));
            params.put("notify_url", payConfig.getNotifyUrl(channelName));
            params.put("front_url", payConfig.getReturnUrl(channelName));
            params.put("body", "支付"+AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))+"元");
            params.put("spbill_create_ip", payOrder.getClientIp());
            params.put("version", "1.0.0");
            params.put("sign_type", "RSA");
            params.put("data", data);

            String signString = XXPayUtil.mapToStringEmity(params);
            PrivateKey privateKey = CryptoUtil.getRSAPrivateKeyByPriKeyStr(channelPayConfig.getRsaprivateKey(), CryptoUtil.keyAlgorithm);
            String sign = CryptoUtil.digitalSign(signString, privateKey, CryptoUtil.signAlgorithm);
            _log.info(logPrefix + "******************sign:{}", sign);
            params.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(params).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}",sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/allpay-api/v1/transPayJ", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String retMsg = resObj.getString("desc");
            boolean retStatus = resObj.getBooleanValue("success");
            if (!retStatus) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payUrl = resObj.getString("content");
            JSONObject payParams = new JSONObject();
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                payParams.put("payJumpUrl", payUrl);
            } else if (payUrl.contains("<form")) {
                payParams.put("payJumpUrl", payUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payJumpUrl", payUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }

            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
        return payInfo;
    }

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【巧巧支付统一下单】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap params = new TreeMap();
            params.put("mer_no", channelPayConfig.getMchId());
            params.put("trans_id", payOrder.getPayOrderId());
            params.put("order_id", payOrder.getChannelOrderNo() != null ? payOrder.getChannelOrderNo() : "");
            params.put("version", "1.0.0");
            params.put("sign_type", "RSA");

            String signString = XXPayUtil.mapToString(params);
            PrivateKey privateKey = CryptoUtil.getRSAPrivateKeyByPriKeyStr(channelPayConfig.getRsaprivateKey(), CryptoUtil.keyAlgorithm);
            String sign = CryptoUtil.digitalSign(signString, privateKey, CryptoUtil.signAlgorithm);
            _log.info(logPrefix + "******************sign:{}", sign);
            params.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(params).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/allpay-api/v1/transQuery",  sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("message");
            String status = resObj.getString("status");

            if (retCode.equals("000000")) {
                retObj.put("status", "2");
                retObj.put("msg", GetStatusMsg(status));
            } else {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String state) {
        switch (state) {
            case "0":
                return "未支付";
            case "1":
                return "支付成功";
            default:
                return "状态不明";
        }
    }

    /// <summary>
    /// 拼接form表单
    /// </summary>
    /// <param name="sParaTemp">键值对</param>
    /// <param name="gateway">请地址</param>
    /// <returns></returns>
    public String buildRequestHtml(Map<String, String> sParaTemp, String gateway) {
        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<html><head></head><body>");
        sbHtml.append("<form id='submitform' action='" + gateway + "' method='post'>");
        for (String key : sParaTemp.keySet()) {
            String value = sParaTemp.get(key);
            sbHtml.append("<input type='hidden' name='" + key + "' value='" + value + "'/>");
        }

        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type='submit' value='submit' style='display:none;'></form>");
        sbHtml.append("<script>document.forms['submitform'].submit();</script></body></html>");
        return sbHtml.toString();
    }


    private String MapConvertStr(SortedMap map) {
        StringBuffer sb = new StringBuffer();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + value.toString());
        }
        return sb.toString();
    }
}
