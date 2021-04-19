package org.xxpay.pay.channel.woerwo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WoerwoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(WoerwoPaymentService.class);

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
                retObj = doMayiPayReq(payOrder, "alipaydfbx");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【沃尔沃支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();

            map.put("price", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//订单金额 元
            map.put("is_type", channel);//支付渠道
            map.put("api_code", channelPayConfig.getMchId());//商户号
            map.put("order_id", payOrder.getPayOrderId());//商户订单号
            map.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//订单时间
            map.put("mark", "HanXin");
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notify_url", payConfig.getNotifyUrl(channelName));
            map.put("return_url", "127.0.0.1");
            map.put("return_type", "json");

            SortedMap<String, String> myMap = XXPayUtil.JSONObjectToSortedMap(map);
            String sign = PayDigestUtil.getSign(myMap, channelPayConfig.getmD5Key());
            map.put("sign", sign.toLowerCase());
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = map.toJSONString();
            _log.info(logPrefix + "******************sendMsg明文:{}", sendMsg);
            String data = map.toJSONString();
            String reqData = MyAES.encryptAES(data, channelPayConfig.getRsaPublicKey(), null);
            JSONObject reqMap = new JSONObject();
            reqMap.put("data", reqData);
            _log.info(logPrefix + "******************sendMsg密文:{}", reqMap.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/interface", reqMap.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resCode = resObj.getString("returncode");
            String resMsg = resObj.getString("returnmsg");
            if (!resCode.equals("SUCCESS")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject dataJson = resObj.getJSONObject("data");
            String payJumpUrl = dataJson.getString("payurl");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            payInfo.put("payParams", payParams);
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
        String logPrefix = "【沃尔沃支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("api_code", channelPayConfig.getMchId());//商户编号
            map.put("order_id", DateUtil.getRevTime());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());

            SortedMap<String, String> myMap = XXPayUtil.JSONObjectToSortedMap(map);
            String sendMsg = XXPayUtil.mapToString(myMap).replace(">", "");
            _log.info(logPrefix + "******************sendMsg明文:{}", sendMsg);

            String data = map.toJSONString();
            String reqData = MyAES.encryptAES(data, channelPayConfig.getRsaPublicKey(), null);
            JSONObject reqMap = new JSONObject();
            reqMap.put("data", reqData);
            _log.info(logPrefix + "******************sendMsg密文:{}", reqMap.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/order_query", reqMap.toJSONString());
            _log.info("上游返回信息：" + res);
            if (res.contains("error")) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败,\n" + "上游返回信息：[" + res + "]");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return payInfo;
            }

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("status");
            if (retCode.equals("1")) {
                // 核对金额
                long outPayAmt = Long.valueOf(AmountUtil.convertDollar2Cent(resObj.getString("order_price")));
                long dbPayAmt = payOrder.getAmount().longValue();
                if (outPayAmt == dbPayAmt) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中,订单状态Code:"+retCode+"。（未收款 = 0，成功 = 1，失败 = 2，关闭 = 3，超时 = 4）");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
