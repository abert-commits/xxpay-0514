package org.xxpay.pay.channel.bifutong;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.core.common.*;

import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class BifutongPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BifutongPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMayiPayReq(payOrder, "pdd-alipay");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
                retObj = doMayiPayReq(payOrder, "pdd-wechat");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【币付通支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("accessKeyId", channelPayConfig.getMchId());//商户编号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳

            //生成6位随机数字
            map.put("nonce", "Abc" + String.valueOf((int) (Math.random() * 900) + 100));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("coinId", "USDT");
            jsonObject.put("address", "0x7168eec15182a7487e7c02245ffb61eaedfb9747");
            jsonObject.put("amount", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));// 单位 元1.00
            jsonObject.put("productName", "HXUSDT");
            jsonObject.put("payType", channel);
            jsonObject.put("accessOrderId", payOrder.getPayOrderId());

            String bodyMd5 = PayDigestUtil.md5(jsonObject.toJSONString(), "UTF-8");
            String sign = PayDigestUtil.md5(channelPayConfig.getMchId() + map.get("nonce") + map.get("timestamp") + bodyMd5 + channelPayConfig.getmD5Key(), "UTF-8");
//            map.put("biz", jsonObject);
            map.put("bodyMd5", bodyMd5);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sign:{}", sign);
//            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToStringByObj(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            Map map1 = new HashMap();
            map1.put("user-agent", "Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; Redmi Note 3 Build/LMY47V) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/46.0.2490.85 Mobile Safari/537.36 XiaoMi/MiuiBrowser/8.1.4");

            String res = PDDUtils.postJson(channelPayConfig.getReqUrl() + "/acceptComp/business/v2/buy?" + sendMsg, jsonObject.toJSONString(), map1);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("message");
            if (!retCode.equals("200")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject dataRes = resObj.getJSONObject("data");
            JSONObject payParams = new JSONObject();
            String channelOrderNo = dataRes.getString("businessId");
            String payJumpUrl = dataRes.getString("sdkCashierURL");
            payJumpUrl = new String(Base64Utils.decode(payJumpUrl));
            payParams.put("payJumpUrl", payJumpUrl);

            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else {
                if (payJumpUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
            }
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), channelOrderNo);
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
        String logPrefix = "【币付通支付订单查询】";
        JSONObject retObj = new JSONObject();
        retObj.put("status", "1");
        retObj.put("msg", "上游没有查询接口！");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }
}
