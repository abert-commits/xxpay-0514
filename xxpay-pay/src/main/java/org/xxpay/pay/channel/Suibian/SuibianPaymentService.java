package org.xxpay.pay.channel.Suibian;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SuibianPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(SuibianPaymentService.class);

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
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【雪支付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
            SortedMap map = new TreeMap();
            map.put("type", channel);
            map.put("total",amount);// 单位 元
            map.put("api_order_sn", payOrder.getPayOrderId());//订单号
            map.put("notify_url", payConfig.getNotifyUrl(channelName));//异步通知地址
            map.put("client_id", channelPayConfig.getMchId());//商户编号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳
            map.put("aisle_name", "HXPDD");

            String signStr = MapConvertStr(map);
            signStr = channelPayConfig.getmD5Key() + signStr + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8");

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/api/order", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("ret");
            String retMsg = resObj.getString("msg");
            _log.info("上游返回信息：" + res);
            if (!code.equals("0")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            //同步返回验签
            SortedMap sortedMap = XXPayUtil.JSONObjectToSortedMap(resObj.getJSONObject("data"));
            String resSign = sortedMap.get("Sign").toString();
            sortedMap.remove("Sign");
            String signRes = PayDigestUtil.getSign(sortedMap, channelPayConfig.getmD5Key()).toLowerCase();
            if (!resSign.equals(signRes)) {
                payInfo.put("errDes", "下单失败,下单同步返回验签失败！");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payUrl = resObj.getJSONObject("data").getString("qr_code_url");
            JSONObject payParams = new JSONObject();
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                payParams.put("payJumpUrl", payUrl);
            } else if (payUrl.contains("form")) {
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
        String logPrefix = "【BBQ支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "2");
            map.put("merchant_number", channelPayConfig.getMchId());//商户编号
            map.put("order_id", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/recharge/queryorder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");

            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            SortedMap resMap = XXPayUtil.JSONObjectToSortedMap(resObj.getJSONObject("data"));
            String resSign = resObj.get("signal").toString();
            //查询返回同步验签
            String signValue = PayDigestUtil.getSign(resMap, channelPayConfig.getmD5Key()).toLowerCase();
            if (!resSign.equals(signValue)) {
                retObj.put("status", "1");
                retObj.put("msg", "查询同步返回，验签失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String state = resMap.get("status").toString();
            String cash = resMap.get("real_cash").toString();
            if (state.equals("2")) {
                // 订单成功
                Long amount = new BigDecimal(cash).multiply(new BigDecimal(100)).longValue();
                if (amount == payOrder.getAmount().longValue()) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", GetStatusMsg(state));
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }

            } else {
                retObj.put("status", "1");
                retObj.put("msg", GetStatusMsg(state));
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
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "支付失败";
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
