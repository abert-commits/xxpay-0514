package org.xxpay.pay.channel.bxpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.sandpay.sdk.AESTool;
import sun.misc.BASE64Encoder;

import java.net.URLEncoder;
import java.util.*;

@Service
public class BxpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BxpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_BX;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XJ:
                retObj = doBxPayReq(payOrder, "1");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doBxPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【Bx通支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("mno", channelPayConfig.getMchId());


            //生成6位随机数字
            JSONObject jsonObject = new JSONObject(16, true);
            jsonObject.put("amount", payOrder.getAmount());// 单位分
            jsonObject.put("device", "1");
            jsonObject.put("mno", channelPayConfig.getMchId());//商户编号
            jsonObject.put("orderno", payOrder.getPayOrderId());
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            jsonObject.put("pt_id", channel);
            jsonObject.put("time", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); //请求时间格式
            String s = XXPayUtil.mapToStringByObj(jsonObject).replace(">", "");
            String rsaprivateKey = channelPayConfig.getRsaprivateKey();
            String signs = Base64Utils.encode(RSASHA256withRSAUtils.encryptByPrivateKey(s.getBytes(), rsaprivateKey));

            _log.info(logPrefix + "******************sign:{}", signs);
            jsonObject.put("sign", signs);
            jsonObject.put("async_notify_url", payConfig.getNotifyUrl(channelName));
            String str = rsaprivateKey.substring(rsaprivateKey.length() - 16, rsaprivateKey.length());

            String content = AESUtils.encryptBase64(jsonObject.toJSONString(), str);
            content = content.replace("\r\n", "");
            map.put("content", URLEncoder.encode(content, "utf-8"));

            String sendMsg = XXPayUtil.mapToStringByObj(map).replace(">", "");

            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/v2/", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("success")) {
                String payJumpUrl = resObj.getJSONObject("data").getString("payurl");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                //表单跳转
                if (resObj.getJSONObject("data").containsKey("sdk_orderstr") && channelPayConfig.getmD5Key() != null && channelPayConfig.getmD5Key().equals("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    payParams.put("payJumpUrl",resObj.getJSONObject("data").getString("sdk_orderstr"));
                } else {
                    if (payJumpUrl.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + res + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
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
        String logPrefix = "【Bx通支付订单查询】";
        JSONObject retObj = new JSONObject();
        retObj.put("status", "1");
        retObj.put("msg", "上游没有查询接口！");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }
}
