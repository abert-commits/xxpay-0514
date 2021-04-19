package org.xxpay.pay.channel.wawajiao;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class WawajiaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(WawajiaoPaymentService.class);

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
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDD:
                retObj = doMayiPayReq(payOrder, "pdd");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "wx");
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【哇哇叫支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("spid", channelPayConfig.getMchId());//商户编号
            map.put("sp_billno", payOrder.getPayOrderId());//商户订单号
            map.put("tran_time", DateUtil.getRevTime());//订单日期
            map.put("pay_type", channel);//支付渠道
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
//            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
//            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("notify_url", payConfig.getNotifyUrl(channelName));
            map.put("tran_amt", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));// 单位 分
            map.put("cur_type", "CNY");

            String signStr = MessageFormat.format("{0}{1}{2}{3}{4}",
                    map.get("spid"), channelPayConfig.getmD5Key(), map.get("tran_amt"), map.get("sp_billno"), map.get("tran_time"));

            String sign = PayDigestUtil.md5(signStr, "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/alipay/transferJson", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.containsKey("message")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("message") + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String code = resObj.getString("statusCode");
            String retMsg = resObj.getString("statusMsg");
            _log.info("上游返回信息：" + res);
            if (!code.equals("0")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            //同步返回验签
            String resSignStr = MessageFormat.format("{0}{1}{2}{3}{4}{5}",
                    resObj.getString("spid"), channelPayConfig.getmD5Key(),
                    resObj.getString("sp_billno"), resObj.getString("listid"),
                    resObj.getString("tran_amt"), resObj.getString("transferDate"));


            String resSign = resObj.getString("sign");
            String signRes = PayDigestUtil.md5(resSignStr, "utf-8").toUpperCase();
            if (!resSign.equals(signRes)) {
                payInfo.put("errDes", "下单失败,下单同步返回验签失败！");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String channelOrderNo = resObj.getString("sp_billno");
            String payUrl = resObj.getString("aliPayUrl");
            String sdkUrl = resObj.getString("sdkUrl");

            JSONObject payParams = new JSONObject();
            if (payOrder.getChannelId().contains("SDK")||payOrder.getChannelId().contains("PDD")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                payParams.put("payJumpUrl", sdkUrl);
            } else if (payUrl.contains("form")) {
                payParams.put("payJumpUrl", payUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payJumpUrl", payUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
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
        String logPrefix = "【哇哇叫支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("spid", channelPayConfig.getMchId());//商户编号
            map.put("sp_billno", payOrder.getPayOrderId());//订单号
            map.put("listid", payOrder.getChannelOrderNo());//上游订单号

            String signStr = MessageFormat.format("{0}{1}{2}{3}",
                    map.get("spid"), channelPayConfig.getmD5Key(), map.get("sp_billno"), map.get("listid"));

            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
            map.put("sign_type", "MD5");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/alipay/queryOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("retcode");
            String retMsg = resObj.getString("retmsg");

            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            //查询返回同步验签
            String resSign = resObj.getString("spid") + channelPayConfig.getmD5Key() + resObj.getString("spBillNo") + resObj.getString("listid") + resObj.getString("tran_amt") + resObj.getString(" create_time");
            String signValue = PayDigestUtil.md5(signStr, "UTF-8");
            if (!resSign.equals(signValue)) {
                retObj.put("status", "1");
                retObj.put("msg", "查询同步返回，验签失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String state = resObj.getString("state");
            if (state.equals("2") || state.equals("3") || state.equals("4")) {
                // 订单成功
                BigDecimal amount = resObj.getBigDecimal("tran_amt");
                if (amount.longValue() == payOrder.getAmount().longValue()) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", GetStatusMsg(state));
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
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
