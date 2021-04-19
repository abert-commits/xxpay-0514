package org.xxpay.pay.channel.yiliuba;

import com.alibaba.fastjson.JSONObject;
import org.apache.el.lang.ELArithmetic;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YiliubaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YiliubaPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝wap
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "1");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【一六八支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("merchno", channelPayConfig.getMchId());
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//订单金额 元
            map.put("traceno", payOrder.getPayOrderId());//商户订单号
            map.put("payType", channel);//支付渠道
//            map.put("goodsName", "HXSHOP");//商品名称
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));
//            map.put("returnUrl", "127.0.0.1");
            map.put("settleType", "0");

            SortedMap<String, String> myMap = XXPayUtil.JSONObjectToSortedMap(map);
            String signStr = XXPayUtil.mapToString(myMap);
            String sign = PayDigestUtil.md5(signStr + "&" + channelPayConfig.getmD5Key(), "GBK");
            map.put("signature", sign.toUpperCase());

            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(XXPayUtil.JSONObjectToSortedMap(map));
            _log.info(logPrefix + "******************sendMsg明文:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/wapPay", sendMsg, "GBK");
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resCode = resObj.getString("respCode");
            String resMsg = resObj.getString("returnmsg");
            if (!resCode.equals("00")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payJumpUrl = resObj.getString("barCode");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            if (payJumpUrl.contains("form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {

                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }
            String channelOrderNo = resObj.getString("refno");
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), channelOrderNo);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return payInfo;

        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
    }


    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【一六八支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap<>();
            map.put("merchno", channelPayConfig.getMchId());//商户编号
            map.put("traceno", payOrder.getPayOrderId());//订单号
            map.put("refno", payOrder.getChannelOrderNo());//上游订单号
            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.getSign(signStr + "&" + channelPayConfig.getmD5Key(), "GBK");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("signature", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map);
            _log.info(logPrefix + "******************sendMsg明文:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/qrcodeQuery", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String respCode = resObj.getString("respCode");
            String message = resObj.getString("message");

            _log.info("上游返回信息：" + res);
            if (!respCode.equals("00")) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败,\n" + "上游返回信息：[" + message + "]");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return payInfo;
            }

            String payStatus = resObj.getString("payStatus");
            retObj.put("status", "1");
            retObj.put("msg", "支付中");
            if (payStatus.equals("2")) {
                retObj.put("status", "2");
                retObj.put("msg", "支付成功");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put("status", "1");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
