package org.xxpay.pay.channel.yuanbao;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YuanbaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YuanbaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QT;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doMayiPayReq(payOrder, "961");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【元宝付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("MerchantsID", channelPayConfig.getMchId());//商户编号
            map.put("OrderID", payOrder.getPayOrderId());//商户订单号
            map.put("Datetime", DateUtil.date2Str(new Date()));//订单日期

            if (StringUtils.isNotBlank(channelPayConfig.getChannelId())) {
                channel = channelPayConfig.getChannelId();
            }
            map.put("ChannelCode", channel);//支付渠道

            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            map.put("asyncUrl", payConfig.getNotifyUrl(channelName));
            map.put("syncUrl", payConfig.getReturnUrl(channelName));
            map.put("money", String.valueOf(amount));// 单位 元

            String md5="ChannelCode="+map.get("ChannelCode")+"&Datetime="+map.get("Datetime")+"" +
                    "&MerchantsID="+map.get("MerchantsID")+"&OrderID="+map.get("OrderID")+"" +
                    "&asyncUrl="+map.get("asyncUrl")+"&money="+map.get("money")+"&syncUrl="+map.get("syncUrl")+"&key="+channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(md5, "utf-8").toUpperCase();

            _log.info(logPrefix + "******************sign:{}", sign);
            String signReverse= new StringBuilder(sign).reverse().toString();
            map.put("signKey",signReverse);
            map.put("returntype", "page");// 单位 元

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Gateway.html", sendMsg);
            _log.info("上游返回信息：" + res);
            /*
            JSONObject resObj = JSONObject.parseObject(res);*/
            if (res.contains("error")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + res + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            JSONObject payParams = new JSONObject();
            String payJumpUrl =res;
            if (payOrder.getChannelId().contains("SDK")) {
                 payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (res.contains("form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }

            payParams.put("payJumpUrl", payJumpUrl);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
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
        String logPrefix = "【元宝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("MerchantsID", channelPayConfig.getMchId());//商户编号
            map.put("OrderID", payOrder.getPayOrderId());//上游訂單號
            map.put("randStr", RandomStringUtils.randomAlphanumeric(32));//上游訂單號
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Index_verify.html", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("status");
//            {"status":"error","msg":"不存在的交易订单号.","data":[]}
            if (resObj.containsKey("status") && resObj.getString("status").equals("error")) {
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
            if (retCode.equals("success")) {
                // 订单成功
                BigDecimal amount = resObj.getJSONObject("data").getBigDecimal("amount");
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
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
