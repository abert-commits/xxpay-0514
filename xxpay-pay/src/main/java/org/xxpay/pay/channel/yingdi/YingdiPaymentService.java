package org.xxpay.pay.channel.yingdi;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YingdiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YingdiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YINGDI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDD: //拼多多支付宝H5
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_PDD: //拼多多微信
                retObj = doMayiPayReq(payOrder, "wepay");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK: //拼多多支付宝SDK
                retObj = doMayiPayReq(payOrder, "alipaysdk");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY: //原生支付宝H5
                retObj = doMayiPayReq(payOrder, "openalipay");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【影帝支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            /*SortedMap map = new TreeMap();
            map.put("noncestr", RandomStringUtils.randomAlphanumeric(32));//随机字符串,长度32位
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//时间戳
            map.put("mid", channelPayConfig.getMchId());//商户ID
            map.put("orderid", payOrder.getPayOrderId());//商户订单号
            map.put("price", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//金额,单位元,2位小数
            map.put("payment", channel);//类型
            map.put("notifyurl",payConfig.getNotifyUrl(getChannelName()));//异步回调地址,不要带?问号
*/
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("noncestr",RandomStringUtils.randomAlphanumeric(32));//随机字符串,长度32位
            jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//时间戳
            jsonObject.put("mid",channelPayConfig.getMchId());//商户ID

            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("orderid", payOrder.getPayOrderId());//商户订单号
            jsonObject1.put("price",AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//金额,单位元,2位小数
            jsonObject1.put("payment",channel);//类型
            jsonObject1.put("notifyurl",payConfig.getNotifyUrl(getChannelName()));//异步回调地址,不要带?问号
            jsonObject.put("data", jsonObject1.toJSONString());

            String sign = PayDigestUtil.getSign(jsonObject, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            jsonObject.put("sign", sign);

            _log.info(logPrefix + "******************sendMsg:{}", jsonObject.toJSONString());

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/CreateOrder", jsonObject.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String message = resObj.getString("message");
            if ("200".equals(code)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {
                    JSONObject data = JSONObject.parseObject(resObj.getString("data"));
                    String payJumpUrl = data.getString("sdk");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payJumpUrl.contains("form")) {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), data.getString("orderid"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                payInfo.put("errDes", "下单失败[" + message + "]");
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
        String logPrefix = "【影帝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
          /*  SortedMap map = new TreeMap();
            map.put("noncestr", RandomStringUtils.randomAlphanumeric(32));//随机字符串,长度32位
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//时间戳
            map.put("mid", channelPayConfig.getMchId());//商户ID

            map.put("orderid", payOrder.getPayOrderId());//商户订单号
            map.put("orderdate",DateUtil.date2Str(new Date(),DateUtil.FORMAT_YYYY_MM_DD));//订单日期Y-m-d
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);*/

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("noncestr",RandomStringUtils.randomAlphanumeric(32)); //随机字符串,长度32位
            jsonObject.put("timestamp",String.valueOf(System.currentTimeMillis() / 1000)); //时间戳
            jsonObject.put("mid",channelPayConfig.getMchId());//商户ID

            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("orderid",payOrder.getPayOrderId());//商户订单号
            jsonObject1.put("orderdate",DateUtil.date2Str(new Date(),DateUtil.FORMAT_YYYY_MM_DD));//订单日期Y-m-d
            jsonObject.put("data", jsonObject1.toJSONString());

            String sign = PayDigestUtil.getSign(jsonObject, channelPayConfig.getmD5Key());
            jsonObject.put("sign", sign);


            _log.info(logPrefix + "******************sendMsg:{}", jsonObject.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/GetOrder", jsonObject.toJSONString());
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String message = resObj.getString("message");
            if ("200".equals(code)) {
                JSONObject data = JSONObject.parseObject(resObj.getString("data"));
                // 订单成功
                BigDecimal amount = data.getBigDecimal("price");
                // 核对金额
                long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                long dbPayAmt = payOrder.getAmount().longValue();
                if (dbPayAmt == outPayAmt) {
                    //支付成功
                   String paystatus=data.getString("paystatus");
                    if(paystatus.equals("1")){
                        retObj.put("status", "2");
                        retObj.put("msg", "支付成功");
                    }else{
                        retObj.put("status", "1");
                        retObj.put("msg", "未支付成功");
                    }
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
