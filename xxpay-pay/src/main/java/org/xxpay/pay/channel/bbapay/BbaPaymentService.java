package org.xxpay.pay.channel.bbapay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.util.Util;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class BbaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BbaPaymentService.class);

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
                retObj = doMayiPayReq(payOrder, "1006");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【BBQ支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("parter", channelPayConfig.getMchId());//商户编号
            map.put("type", channel);//支付类型
            map.put("value", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));// 单位 元1.00
            map.put("orderid", payOrder.getPayOrderId());//订单号
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("callbackurl", payConfig.getNotifyUrl(channelName));
//            map.put("hrefbackurl", "127.0.0.1");


            String signStr = MessageFormat.format("parter={0}&type={1}&value={2}&orderid={3}&callbackurl={4}{5}",
                    map.get("parter"), map.get("type"), map.get("value"), map.get("orderid"), map.get("callbackurl"), channelPayConfig.getmD5Key());

            String sign = PayDigestUtil.md5(signStr, "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "//pay.aspx", sendMsg);
//            JSONObject resObj = JSONObject.parseObject(res);
//            if (resObj.containsKey("message")) {
//                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("message") + "]");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }
//
//            String code = resObj.getString("statusCode");
//            String retMsg = resObj.getString("statusMsg");
//            _log.info("上游返回信息：" + res);
//            if (!code.equals("0")) {
//                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }
//
//            //同步返回验签
//            String resSignStr = MessageFormat.format("{0}{1}{2}{3}{4}{5}",
//                    resObj.getString("spid"), channelPayConfig.getmD5Key(),
//                    resObj.getString("sp_billno"), resObj.getString("listid"),
//                    resObj.getString("tran_amt"), resObj.getString("transferDate"));
//
//
//            String resSign = resObj.getString("sign");
//            String signRes = PayDigestUtil.md5(resSignStr, "utf-8").toUpperCase();
//            if (!resSign.equals(signRes)) {
//                payInfo.put("errDes", "下单失败,下单同步返回验签失败！");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }
//
//            String channelOrderNo = resObj.getString("sp_billno");
//            String payUrl = resObj.getString("aliPayUrl");
//            String sdkUrl = resObj.getString("sdkUrl");
//
//
//            if (payOrder.getChannelId().contains("SDK")||payOrder.getChannelId().contains("PDD")) {
//                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
//                payParams.put("payJumpUrl", sdkUrl);
//            } else if (payUrl.contains("form")) {
//                payParams.put("payJumpUrl", payUrl);
//                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
//            } else {
//                payParams.put("payJumpUrl", payUrl);
//                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
//            }
            JSONObject payParams = new JSONObject();
            if (res.contains("error")) {
                payInfo.put("errDes", "下单失败，失败信息：" + URLDecoder.decode(res, "GBK"));
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            payParams.put("payJumpUrl", res);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
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
        String logPrefix = "【BBA支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("parter", channelPayConfig.getMchId());//商户编号
            map.put("orderid", payOrder.getPayOrderId());//订单号

            String signStr = MessageFormat.format("parter={0}&orderid={1}{2}",
                    map.get("parter"), map.get("orderid"), channelPayConfig.getmD5Key());

            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/query.ashx?" + sendMsg);
            Map resMap = XXPayUtil.convertParamsString2Map(res);

            String resSignStr = MessageFormat.format("orderid={0}&opstate={1}&ovalue={2}{3}",
                    resMap.get("orderid"), resMap.get("opstate"), resMap.get("ovalue"), channelPayConfig.getmD5Key());
            String resSign = PayDigestUtil.md5(resSignStr, "UTF-8");

            _log.info("上游返回信息：" + res);
            String retCode = resMap.get("opstate").toString();
            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }


            String s = resMap.get("sign").toString().replace("\r", "").replace("\n", "");
            if (!resSign.equals(s)) {
                retObj.put("status", "1");
                retObj.put("msg", "查询同步返回，验签失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            //支付成功
            retObj.put("status", "2");
            retObj.put("msg", "支付成功");
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
                return "创建未支付";
            case "2":
                return "已支付-待回调";
            case "3":
                return "已支付-回调失败";
            case "4":
                return "已支付-已回调";
            case "5":
                return "订单关闭(支付超时)";
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
}
