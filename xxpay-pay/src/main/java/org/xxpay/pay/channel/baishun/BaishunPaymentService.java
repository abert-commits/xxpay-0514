package org.xxpay.pay.channel.baishun;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class BaishunPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BaishunPaymentService.class);

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
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMayiPayReq(payOrder, "422");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }

        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【百顺支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("mchid", Integer.valueOf(channelPayConfig.getMchId()));//商户编号
            String timesTamp = String.valueOf(System.currentTimeMillis());
            timesTamp = timesTamp.substring(0, timesTamp.length() - 3);
            map.put("timestamp", timesTamp);
            map.put("nonce", DateUtil.getRevTime().substring(0, 4));

            JSONObject dataMap = new JSONObject();
            dataMap.put("paytype", Integer.valueOf(channel));//413
            dataMap.put("out_trade_no", payOrder.getPayOrderId());//商户订单号
            dataMap.put("goodsname", "HXShoop");
            dataMap.put("total_fee", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//订单日期
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            dataMap.put("notify_url", payConfig.getNotifyUrl(channelName));
            dataMap.put("return_url", "http://wwww.baidu.com");
            dataMap.put("requestip", "120.79.101.38");

            String signStr = MessageFormat.format("goodsname={0}&mchid={1}&nonce={2}&notify_url={3}&out_trade_no={4}&paytype={5}&requestip={6}&return_url={7}&timestamp={8}&total_fee={9}&key={10}"
                    , dataMap.get("goodsname"), map.get("mchid").toString(), map.get("nonce"), dataMap.get("notify_url"), dataMap.getString("out_trade_no"), dataMap.get("paytype"), dataMap.get("requestip"), dataMap.get("return_url"), map.get("timestamp"), dataMap.get("total_fee"), channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            map.put("data", dataMap);
            String sendMsg = map.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay/gopay", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("error");
            String retMsg = resObj.getString("msg");
            if (!retCode.equals("0")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject dataResObj = resObj.getJSONObject("data");
//            String resSignStr = MessageFormat.format("createdate={0}&error={1}&mark={2}&out_trade_no={3}&paytype={4}&payurl={5}&serverTime={6}&total_fee={7}&trade_no={8}&key={9}"
//                    , dataResObj.getString("createdate"), resObj.getString("error"), dataResObj.getString("mark"), dataResObj.getString("out_trade_no"), dataResObj.getString("paytype")
//                    , dataResObj.getString("payurl"), resObj.getString("serverTime"), dataResObj.getString("total_fee"), dataResObj.getString("trade_no"), channelPayConfig.getmD5Key());
//
//
//            String resSign = PayDigestUtil.md5(resSignStr, "UTF-8");
//            if (resSign != resObj.getString("sign")) {
//                payInfo.put("errDes", "下单失败,\n" + "失败信息：[同步返回验签失败]");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }

            JSONObject payParams = new JSONObject();
            String payJumpUrl = dataResObj.getString("payurl");
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

            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
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
        String logPrefix = "【百顺支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("mchid", Integer.valueOf(channelPayConfig.getMchId()));//商户编号
            String timesTamp = String.valueOf(System.currentTimeMillis());
            timesTamp = timesTamp.substring(0, timesTamp.length() - 3);
            map.put("timestamp", timesTamp);//时间戳
            map.put("nonce", DateUtil.getRevTime().substring(0, 4));

            JSONObject dataMap = new JSONObject();
            dataMap.put("trade_no", payOrder.getPayOrderId());
            String signStr = MessageFormat.format("mchid={0}&nonce={1}&timestamp={2}&trade_no={3}&key={4}"
                    , channelPayConfig.getMchId(), map.get("nonce"), map.get("timestamp"), dataMap.get("trade_no"), channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            map.put("sign", sign);
            map.put("data", dataMap);
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = map.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay/orderinfo", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("error");
            String retMsg = resObj.getString("msg");

            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            JSONObject dataResObj = resObj.getJSONObject("data");
            String state = dataResObj.getString("state");
            if (state.equals("3")) {
                // 订单成功
                BigDecimal amount = dataResObj.getBigDecimal("total_fee");
                // 核对金额
                long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                long dbPayAmt = payOrder.getAmount().longValue();
                if (dbPayAmt == payOrder.getAmount()) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
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
            retObj.put("status", "1");
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
