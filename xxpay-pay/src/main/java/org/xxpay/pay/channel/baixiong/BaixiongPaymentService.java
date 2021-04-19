package org.xxpay.pay.channel.baixiong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

@Service
public class BaixiongPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BaixiongPaymentService.class);

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
        String logPrefix = "【白熊支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {

            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map<String,String> signParams = new HashMap<>();
            signParams.put("mno", channelPayConfig.getMchId());
            signParams.put("orderno", payOrder.getPayOrderId());
            signParams.put("amount",String.valueOf(payOrder.getAmount()));
            signParams.put("pt_id", channelPayConfig.getChannelId());
            signParams.put("code","1018");
            signParams.put("time", DateUtil.getCurrentTimeStr(DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
            //使用 RSA 1024 非对称加密公钥私钥对生成签名 签名字段进行键名字母排序(同步通知 notify_url;异步通知 async_notify_url 无需加入签名字段)，
            // 其他 content 请求参数 都需进行生成商户 sign 与服务端 sign 进行匹配
            signParams.put("sign", SignUtils.getSign(signParams,channelPayConfig.getRsaprivateKey()));
            signParams.put("async_notify_url",payConfig.getNotifyUrl(channelName));
            _log.info(logPrefix + "******************sign:{}", signParams);
            _log.info(logPrefix + "******************content:{}", JSON.toJSONString(SignUtils.sortMapByKey(signParams)));

            String content = SignUtils.getContent(signParams,channelPayConfig.getmD5Key());


            Map<String,String> params = new HashMap<>();
            params.put("mno",channelPayConfig.getMchId());
            params.put("content", URLEncoder.encode(content,"utf-8"));
            String sendMsg = XXPayUtil.mapToString(params);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/v2/", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (!code.equals("success")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payUrl = resObj.getJSONObject("data").getString("payurl");
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
        String logPrefix = "【白熊支付统一下单】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            Map<String,String> signParams = new HashMap<>();
            signParams.put("mno", channelPayConfig.getMchId());
            //createOrder 接口生成的订单号
            signParams.put("orderno", payOrder.getPayOrderId());
            signParams.put("amount", String.valueOf(payOrder.getAmount()));
            signParams.put("time",SignUtils.getDateTime_YYYY_MM_DD_HH_MM_SS());

            //使用 RSA 1024 非对称加密公钥私钥对生成签名 签名字段进行键名字母排序(同步通知 notify_url;异步通知 async_notify_url 无需加入签名字段)，
            // 其他 content 请求参数 都需进行生成商户 sign 与服务端 sign 进行匹配
            signParams.put("sign", SignUtils.getSign(signParams,channelPayConfig.getRsaprivateKey()));
            String content = SignUtils.getContent(signParams,channelPayConfig.getmD5Key());
            Map<String,Object> params = new HashMap<>();
            params.put("mno",channelPayConfig.getMchId());
            params.put("content", content);
            String sendMsg = JSONObject.toJSONString(params);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/v2/status/", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("success")) {
                retObj.put("status", "2");
                retObj.put("msg", GetStatusMsg(resObj.getJSONObject("data").getString("status")));
            } else {
                retObj.put("status", "1");
                retObj.put("msg", GetStatusMsg(resObj.getJSONObject("data").getString("status")));
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
            case "1":
                return "支付成功";
            case "2":
                return "未支付";
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
