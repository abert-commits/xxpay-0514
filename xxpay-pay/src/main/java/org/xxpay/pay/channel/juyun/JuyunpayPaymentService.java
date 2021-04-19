package org.xxpay.pay.channel.juyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class JuyunpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JuyunpayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUNYUN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJuYunRyPayReq(payOrder, "jfali"); //支付宝WAP h5 jfali,jfwx可填任意值
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuYunRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【聚云支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            byte[] context = null;
            JSONObject jsonObject = new JSONObject();
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            jsonObject.put("merchNo", channelPayConfig.getMchId());//商户号
            jsonObject.put("orderNo", payOrder.getPayOrderId());// 商户订单号
            jsonObject.put("outChannel", pay_code);//支付类型
            jsonObject.put("bankCode", "1001");//银行编号
            jsonObject.put("title", "消费");//支付标题
            jsonObject.put("product", "消费");//商品描述
            jsonObject.put("memo", "消费");//商品备注
            jsonObject.put("amount", String.valueOf(amount));// 金额
            jsonObject.put("currency", "CNY");//币种
            jsonObject.put("returnUrl", payConfig.getReturnUrl(channelName));// 同步跳转地址
            jsonObject.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            jsonObject.put("reqTime", DateUtil.getSeqString()); //请求时间
            jsonObject.put("userId", "11111");
            context = JSON.toJSONString(jsonObject).getBytes("UTF-8");
            String sign = PayDigestUtil.md5(new String(getContentBytes(new String(context, "UTF-8") + channelPayConfig.getmD5Key(), "UTF-8"), "UTF-8"), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            JSONObject jo = new JSONObject();
            jo.put("sign", sign);
            jo.put("context", context);
            jo.put("encryptType", "MD5");
            _log.info(logPrefix + "******************sendMsg:{}", jsonObject.toString());
            JSONObject jsonObj = new JSONObject(jsonObject);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/order", jo.toJSONString());

            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("0")) {
                String context1 = resObj.getString("context");
                String json1 = new String(Base64Utils.decode(context1.getBytes("UTF-8")), "UTF-8");
                JSONObject jsonObj1 = JSONObject.parseObject(json1);

                String payDinghongUrl = jsonObj1.getString("qrcode_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payDinghongUrl);
                //表单跳转
                if (payDinghongUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
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

    public static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错诿,指定的编码集不对,您目前指定的编码集是:" + charset);
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
        String logPrefix = "【聚云支付订单查询】";
        JSONObject jsonObject = new JSONObject();
        JSONObject retObj = new JSONObject();
        byte[] context = null;
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            jsonObject.put("orderNo", payOrder.getPayOrderId());// 订单号
            jsonObject.put("merchNo", channelPayConfig.getMchId()); // 商户号
            context = JSON.toJSONString(jsonObject).getBytes("UTF-8");
            String sign = PayDigestUtil.md5(new String(getContentBytes(new String(context, "UTF-8") + channelPayConfig.getmD5Key(), "UTF-8"), "UTF-8"), "UTF-8");
            JSONObject jo = new JSONObject();
            jo.put("sign", sign);
            jo.put("context", context);
            jo.put("encryptType", "MD5");
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/order/query", jo.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            ChannelPayConfig channelPayConfigs = new ChannelPayConfig(getPayParam(payOrder));
            if (code.equals("0")) {
                String signs = resObj.getString("sign");
                String contexts = resObj.getString("context");
                String json1 = new String(Base64Utils.decode(contexts.getBytes("UTF-8")), "UTF-8");
                ChannelPayConfig channelPayConfig1 = new ChannelPayConfig(getPayParam(payOrder));
                String rsign = PayDigestUtil.md5(new String(JuyunpayPaymentService.getContentBytes(json1 + channelPayConfig1.getmD5Key(), "UTF-8"), "UTF-8"), "UTF-8");
                //核对验签
                if (!signs.equals(rsign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
                if (resObj.getString("code").equals("0")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
                String status = resObj.getString("code");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            }
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "支付成功";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
