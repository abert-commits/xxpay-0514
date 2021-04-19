package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.RefundOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.mq.rocketmq.normal.Mq5CashColl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝转发业务帮助类
 *
 * @author: xiao
 * @date: 17/12/24
 * @description:
 */
@Service
public class AlitstpayPaymentService extends BasePayment {
    private final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOnpyVEOvANpRu8lqwhLLxM+oFCDA6DmiReD6v2rKwkSz/s+k88QdN4qaDgdT66PCQGIomPU7N0mN3tJXiWEo8mUxAUD7jyZkDbysyGot9JEbAtDdWrmIe3UE8MvDAW02ahBfm4ZrT8E6mt4Lzqp8aAQ0FqsFJkR1XbgpJ/ITDjtAgMBAAECgYEA15mK21GPbj19CjRX/p791ukkbtEzaPzUY35N/F3mnsheNx+osXRjo9rGkOJDbYudK3K66vV5FSWCgfpP8pjdNM7ycahKwFg952MdtNpty3zpyFDkcWeMjVBSso5wtyqvgILfr4qgS3aHMintLSwSQtUjt5DhulzSyX3Z5YsAI8ECQQD2an3sAsSz4z6RfGlIr5tJc5Mi300btwpROGNmUVxAwbqpqq0UeSOgJEzmoMburS7X/v82Dn4QIfGQc0FZk3mlAkEA8wLOsgUUfsJfp7zcdxwD135EDnYm9jRib2vk9DBGulKV1MkbPMF8Z/3yn9UySH8b8pm3y7o7Ur5iuFb84xtPqQJBANQufp9rAtWjJ40/A6mDDMQCsP+mKE9lHY0ycOT5yeY46vKN9NtcNEEBAPbWGnYKyftTp450jDh4AfnQRMVNJ8ECQQCZ8eRZGCjEqIQKcfVEK2YvpJiehLDn9YWKSlKPcunLbTfnxcLQeU5DXrfOEzQ4gvWEeWba085y+5L0bn7jrFCJAkACF7+rox6W+wkuTfrSp4JAf0brfWwJhTV9QhKAYOzPjHU3xQHPcLq8h9rUitBbo1k8gfP+CW0lve7OZf0ecJl5";
    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    private static final MyLog _log = MyLog.getLog(AlitstpayPaymentService.class);
    public final static String PAY_CHANNEL_ALIPAY_QR_H5 = "alipay_qr_h5";                // 支付宝当面付之H5支付
    public final static String PAY_CHANNEL_ALIPAY_QR_PC = "alipay_qr_pc";                // 支付宝当面付之PC支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_APP = "alipay_coupon_app";     // 支付宝红包无线支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_PAGE = "alipay_coupon_page";   // 支付宝红包页面支付

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject payInfo = new JSONObject();
        String channelName = payOrder.getPayOrderId().substring(payOrder.getPayOrderId().indexOf("_") + 1);
        try {
            String logPrefix = "【支付宝" + channelName + "支付下单】";
            String payOrderId = payOrder.getPayOrderId();
            _log.info("{}开始支付宝下单,payOrderId={}", logPrefix, payOrderId);
            //如果是现金红包，去拿发起红包支付需要绑定的PID
            if (payOrder.getChannelId().contains("coupon")) {
                String playerPid = GetRandMPid(payOrder.getPayOrderId());
                payOrder.setParam2(playerPid);
                _log.info("现金红包发起人PID:{}, payOrderId={}", playerPid, payOrderId);
            }

            AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
            String payOrderJson = JSONObject.toJSONString(payOrder);
            _log.info("调用中转服务发起订单支付， payOrderId={},payOrderJson明文:{}", payOrderId, payOrderJson);
            _log.info("调用中转服务发起订单支付， payOrderId={},alipayConfig明文:{}", payOrderId, getPayParam(payOrder));
            byte[] data = payOrderJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);

            byte[] alipayConfigByte = getPayParam(payOrder).getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String payOrderRsa = Base64Utils.encode(encodedData);

            _log.info("调用中转服务发起订单支付， payOrderId={},payOrderJson密文:{}", payOrderId, payOrderRsa);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            _log.info("调用中转服务发起订单支付， payOrderId={},alipayConfig明文:{}", payOrderId, alipayConfigRsa);

            Map map = new HashMap();
            map.put("payOrder", payOrderRsa);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = "";
            if (alipayConfig.getAlipayPublicKey() != null && StringUtils.isNotBlank(alipayConfig.getAlipayPublicKey())) {
                res = XXPayUtil.doPostQueryCmd(alipayConfig.getAlipayPublicKey() + "/aliPay/pay", sendMsg);
            } else {
                res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/pay", sendMsg);
            }

            JSONObject resObj = JSONObject.parseObject(res);
            if (!resObj.getString(PayConstant.RETURN_PARAM_RETCODE).equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString(PayConstant.RETURN_PARAM_RETMSG) + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payUrl = resObj.getString("payUrl");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payUrl);
            if (payOrder.getChannelId().contains("mobile")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else {
                if (payUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);

                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
            }

            String tradeNo = resObj.getString("tradeNo");
            rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), tradeNo);
            payInfo.put("payParams", payParams);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return payInfo;
        } catch (Exception EX) {
            EX.printStackTrace();
            _log.error("订单号：" + payOrder.getPayOrderId() + ",异常信息:" + EX.getStackTrace() + EX.getMessage());
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
    }

    @Autowired
    private Mq5CashColl mq5CashColl;

    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【支付宝订单查询】";
        JSONObject resObj = null;
        try {

            _log.info("{}开始查询支付宝通道订单,payOrderId={}", logPrefix, payOrder.getPayOrderId());
            String payConfJson = getPayParamAll(payOrder);
            AlipayConfig alipayConfig = new AlipayConfig(payConfJson);
            String payOrderJson = JSONObject.toJSONString(payOrder);
            byte[] data = payOrderJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            byte[] alipayConfigByte = getPayParam(payOrder).getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String payOrderRsa = Base64Utils.encode(encodedData);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            _log.info("参数信息，payOrder=》{},alipayConfig=》{}", payOrderJson, payConfJson);
            Map map = new HashMap();
            map.put("payOrder", payOrderRsa);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/query", sendMsg);
            resObj = JSONObject.parseObject(res);
            return resObj;

        } catch (Exception ex) {

            resObj = new JSONObject();
            resObj.put("status", 1);
            resObj.put("errDes", "操作失败!");
            resObj.put("msg", "查询系统：请求上游发生异常！");
            return resObj;
        }


    }

    @Override
    public JSONObject close(PayOrder payOrder) {
        return null;
    }

    /**
     * 支付宝wap支付
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayWapReq(PayOrder payOrder) {
        String logPrefix = "【支付宝WAP支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_WAP_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("quit_url"))) {
                    model.setQuitUrl(objParamsJson.getString("quit_url"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String payUrl = null;
        JSONObject retObj = buildRetObj();
        try {
            String body = client.pageExecute(alipay_request).getBody();
            //payUrl = buildWapUrl(body);
            payUrl = body;
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);

        if (StringUtils.isBlank(payUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");


        retObj.put("payOrderId", payOrderId);
        JSONObject payParams = new JSONObject();
        payParams.put("payJumpUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
    }


    /**
     * 支付宝pc支付
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayPcReq(PayOrder payOrder) {
        String logPrefix = "【支付宝PC支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradePagePayRequest alipay_request = new AlipayTradePagePayRequest();
        // 封装请求支付信息
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        String qr_pay_mode = "2";
        String qrcode_width = "200";
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                qr_pay_mode = ObjectUtils.toString(objParamsJson.getString("qr_pay_mode"), "2");
                qrcode_width = ObjectUtils.toString(objParamsJson.getString("qrcode_width"), "200");
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        model.setQrPayMode(qr_pay_mode);
        model.setQrcodeWidth(Long.parseLong(qrcode_width));
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String payUrl = null;
        JSONObject retObj = buildRetObj();
        try {
            payUrl = client.pageExecute(alipay_request).getBody();
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);

        if (StringUtils.isBlank(payUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        retObj.put("payOrderId", payOrderId);
        JSONObject payParams = new JSONObject();
        payParams.put("payUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
    }

    /**
     * 支付宝手机支付 SDK
     * TODO 待测试
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayMobileReq(PayOrder payOrder) {
        JSONObject payInfo = new JSONObject();
        try {
            String logPrefix = "【支付宝APP支付下单】";
            String payOrderId = payOrder.getPayOrderId();
            AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
            String payOrderJson = JSONObject.toJSONString(payOrder);
            byte[] payOrderByte = payOrderJson.getBytes("utf-8");
            String payOrderRSA = new String(MyRSAUtils.encryptDataByPublicKey(payOrderByte, publicKey), "utf-8");
            byte[] alipayConfigByte = getPayParam(payOrder).getBytes("utf-8");
            String alipayConfigRSA = new String(MyRSAUtils.encryptDataByPublicKey(alipayConfigByte, publicKey), "utf-8");
            Map map = new HashMap();
            map.put("payOrder", payOrderRSA);
            map.put("alipayConfig", alipayConfigRSA);
            String payUrl = buildRequestHtml(map, alipayConfig.getReqUrl());
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payUrl);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return payInfo;
        } catch (Exception EX) {
            _log.error("订单号：" + payOrder.getPayOrderId() + ",异常信息:" + EX.getStackTrace() + EX.getMessage());
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
    }

    /**
     * 支付宝当面付(扫码)支付
     * 收银员通过收银台或商户后台调用支付宝接口，生成二维码后，展示给用户，由用户扫描二维码完成订单支付。
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayQrReq(PayOrder payOrder) {
        String logPrefix = "【支付宝当面付之扫码支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if (StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String aliResult;
        String codeUrl = "";
        JSONObject retObj = buildRetObj();
        try {
            aliResult = client.execute(alipay_request).getBody();
            JSONObject aliObj = JSONObject.parseObject(aliResult);
            JSONObject aliResObj = aliObj.getJSONObject("alipay_trade_precreate_response");
            codeUrl = aliResObj.getString("qr_code");
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成支付宝二维码：codeUrl={}", logPrefix, codeUrl);

        if (StringUtils.isBlank(codeUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        retObj.put("payOrderId", payOrderId);
        JSONObject payInfo = new JSONObject();
        payInfo.put("codeUrl", codeUrl); // 二维码支付链接
        payInfo.put("codeImgUrl", payConfig.getPayUrl() + "/qrcode_img_get?url=" + codeUrl + "&widht=200&height=200");
        payInfo.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
        retObj.put("payParams", payInfo);
        _log.info("###### 商户统一下单处理完成 ######");
        return retObj;
    }


    /**
     * 支付宝当面付(PC)支付
     * 收银员通过收银台或商户后台调用支付宝接口，生成二维码后，展示给用户，由用户扫描二维码完成订单支付。
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayQrPcReq(PayOrder payOrder, String type) {
        String logPrefix = "【支付宝当面付之PC支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if (StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String aliResult;
        String codeUrl = "";
        JSONObject retObj = buildRetObj();
        try {
            aliResult = client.execute(alipay_request).getBody();
            JSONObject aliObj = JSONObject.parseObject(aliResult);
            JSONObject aliResObj = aliObj.getJSONObject("alipay_trade_precreate_response");
            codeUrl = aliResObj.getString("qr_code");
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成支付宝二维码：codeUrl={}", logPrefix, codeUrl);
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);

        String codeImgUrl = payConfig.getPayUrl() + "/qrcode_img_get?url=" + codeUrl + "&widht=200&height=200";
        StringBuffer payForm = new StringBuffer();
        String toPayUrl = payConfig.getPayUrl() + "/alipay/pay_" + type + ".htm";
        payForm.append("<form style=\"display: none\" action=\"" + toPayUrl + "\" method=\"post\">");
        payForm.append("<input name=\"mchOrderNo\" value=\"" + payOrder.getMchOrderNo() + "\" >");
        payForm.append("<input name=\"payOrderId\" value=\"" + payOrder.getPayOrderId() + "\" >");
        payForm.append("<input name=\"amount\" value=\"" + payOrder.getAmount() + "\" >");
        payForm.append("<input name=\"codeUrl\" value=\"" + codeUrl + "\" >");
        payForm.append("<input name=\"codeImgUrl\" value=\"" + codeImgUrl + "\" >");
        payForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
        payForm.append("</form>");
        payForm.append("<script>document.forms[0].submit();</script>");

        retObj.put("payOrderId", payOrderId);
        JSONObject payInfo = new JSONObject();
        payInfo.put("payUrl", payForm);
        payInfo.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payInfo);
        _log.info("###### 商户统一下单处理完成 ######");
        return retObj;
    }


    /**
     * 支付宝当面付(H5)支付
     * 收银员通过收银台或商户后台调用支付宝接口，可直接打开支付宝app付款。
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayQrH5Req(PayOrder payOrder, String type) {
        String logPrefix = "【支付宝当面付之H5支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if (StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if (StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String aliResult;
        String codeUrl = "";
        JSONObject retObj = buildRetObj();
        try {
            aliResult = client.execute(alipay_request).getBody();
            JSONObject aliObj = JSONObject.parseObject(aliResult);
            JSONObject aliResObj = aliObj.getJSONObject("alipay_trade_precreate_response");
            codeUrl = aliResObj.getString("qr_code");
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成支付宝二维码：codeUrl={}", logPrefix, codeUrl);
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);

        String codeImgUrl = payConfig.getPayUrl() + "/qrcode_img_get?url=" + codeUrl + "&widht=200&height=200";
        StringBuffer payForm = new StringBuffer();
        String toPayUrl = payConfig.getPayUrl() + "/alipay/pay_" + type + ".htm";
        payForm.append("<form style=\"display: none\" action=\"" + toPayUrl + "\" method=\"post\">");
        payForm.append("<input name=\"mchOrderNo\" value=\"" + payOrder.getMchOrderNo() + "\" >");
        payForm.append("<input name=\"payOrderId\" value=\"" + payOrder.getPayOrderId() + "\" >");
        payForm.append("<input name=\"amount\" value=\"" + payOrder.getAmount() + "\" >");
        payForm.append("<input name=\"codeUrl\" value=\"" + codeUrl + "\" >");
        payForm.append("<input name=\"codeImgUrl\" value=\"" + codeImgUrl + "\" >");
        payForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
        payForm.append("</form>");
        payForm.append("<script>document.forms[0].submit();</script>");

        retObj.put("payOrderId", payOrderId);
        JSONObject payInfo = new JSONObject();
        payInfo.put("payUrl", payForm);
        payInfo.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payInfo);

        _log.info("###### 商户统一下单处理完成 ######");
        return retObj;
    }

    /**
     * 支付宝红包无线支付
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayCouponAppReq(PayOrder payOrder) {
        String logPrefix = "【支付宝红包无线支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayFundCouponOrderAppPayRequest alipay_request = new AlipayFundCouponOrderAppPayRequest();
        // 封装请求支付信息
        AlipayFundCouponOrderAppPayModel model = new AlipayFundCouponOrderAppPayModel();
        model.setOutOrderNo(payOrderId);
        model.setOutRequestNo(payOrderId);
        model.setOrderTitle(payOrder.getSubject());
        model.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setPayTimeout("2h");

        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String payUrl = null;
        JSONObject retObj = buildRetObj();
        try {
            String body = client.pageExecute(alipay_request).getBody();
            //payUrl = buildWapUrl(body);
            payUrl = body;
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);

        if (StringUtils.isBlank(payUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");

        retObj.put("payOrderId", payOrderId);
        JSONObject payParams = new JSONObject();
        payParams.put("payUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
    }

    /**
     * 支付宝红包页面支付
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayCouponPageReq(PayOrder payOrder) {
        String logPrefix = "【支付宝红包页面支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayFundCouponOrderPagePayRequest alipay_request = new AlipayFundCouponOrderPagePayRequest();
        // 封装请求支付信息
        AlipayFundCouponOrderPagePayModel model = new AlipayFundCouponOrderPagePayModel();
        model.setOutOrderNo(payOrderId);
        model.setOutRequestNo(payOrderId);
        model.setOrderTitle(payOrder.getSubject());
        model.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setPayTimeout("2h");

        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(payConfig.getNotifyUrl(getChannelName()));
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String payUrl = null;
        JSONObject retObj = buildRetObj();
        try {
            String body = client.pageExecute(alipay_request).getBody();
            //payUrl = buildWapUrl(body);
            payUrl = body;
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("errDes", "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);

        if (StringUtils.isBlank(payUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");

        retObj.put("payOrderId", payOrderId);
        JSONObject payParams = new JSONObject();
        payParams.put("payUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
    }

    /**
     * 生成支付宝wap支付url,解析html
     *
     * @param formHtml
     * @return
     */
    String buildWapUrl(String formHtml) {
        Document doc = Jsoup.parse(formHtml);
        Elements formElements = doc.getElementsByTag("form");
        Element formElement = formElements.get(0);
        String action = formElement.attr("action");
        String biz_content = "";
        Elements inputElements = formElement.getElementsByTag("input");
        for (Element inputElement : inputElements) {
            String name = inputElement.attr("name");
            String value = inputElement.attr("value");
            if ("biz_content".equals(name)) {
                biz_content = value;
                biz_content = value.replaceAll("&quot;", "\"");
                break;
            }
        }
        return action + "&biz_content=" + biz_content;
    }


    /**
     * 现金红包分发
     *
     * @param payOrder
     * @return
     */

    public JSONObject transunitransferByTST(PayOrder payOrder) {
        JSONObject retObj = buildRetObj();
        String logPrefix = "【支付宝红包无线支付现金红包分发】";

        String payOrderId = payOrder.getPayOrderId();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(logPrefix);
        try {

            AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
            PayCashCollConfig payCashCollConfig = QueryPayCashCollConfig(payOrder.getPassageAccountId().toString(), MchConstant.PUB_YES);
            if (payCashCollConfig == null) {
                stringBuffer.append("订单号：" + payOrderId + ",分发现金红包失败：未获取到收红包的账号信息");
                retObj.put("errDes", "分发现金红包失败,未获取到收红包的账号信息");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            }

            payOrder.setPayOrderId(String.valueOf(System.currentTimeMillis()));//临时赋值，用于请求支付宝
            String payOrderJson = JSONObject.toJSONString(payOrder);
            stringBuffer.append("订单号：" + payOrderId + ",payOrderJson明文：" + payOrderJson);
            String payCashCollConfigJson = JSONObject.toJSONString(payCashCollConfig);
            byte[] data = payOrderJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            String payOrderRsa = Base64Utils.encode(encodedData);

            byte[] alipayConfigByte = getPayParam(payOrder).getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);


            byte[] payCashCollConfigBytes = payCashCollConfigJson.getBytes();
            byte[] encodedDataByPayCashColl = RSAUtils.encryptByPublicKey(payCashCollConfigBytes, publicKey);
            String payCashCollRsa = Base64Utils.encode(encodedDataByPayCashColl);
            stringBuffer.append("订单号：" + payOrderId + ",payCashCollConfigJson明文：" + payCashCollConfigJson);


            Map map = new HashMap();
            map.put("payOrder", payOrderRsa);
            map.put("alipayConfig", alipayConfigRsa);
            map.put("payCashCollConfig", payCashCollRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/transunitransfer", sendMsg);
            _log.info("订单号：{}，{},响应信息：{}", payOrderId, logPrefix, res);
            retObj = JSONObject.parseObject(res);

            //插入资金归集
            PayOrderCashCollRecord record = new PayOrderCashCollRecord();
            record.setPayOrderId(payOrderId);
            record.setTransInUserId(payCashCollConfig.getTransInUserId());
            record.setChannelOrderNo(payOrder.getChannelOrderNo());
            record.setTransInPercentage(payCashCollConfig.getTransInPercentage());
            record.setTransInAmount(payOrder.getAmount());
            record.setTransInUserAccount(payCashCollConfig.getTransInUserAccount());
            record.setCreateTime(new Date());
            record.setRemark("现金红包领取成功");
            record.setType(2);
            record.setTransInUserName(payCashCollConfig.getTransInUserName());
            record.setStatus((byte) 1);
            record.setPassageAccountId(payOrder.getPassageAccountId().toString());

            if (!retObj.getString(PayConstant.RETURN_PARAM_RETCODE).equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                record.setRemark(retObj.getString("errDes"));
                record.setStatus((byte) 0);
            }
            rpcCommonService.rpcPayOrderCashCollRecordService.add(record);
            return retObj;

        } catch (Exception ex) {
            ex.printStackTrace();
            retObj.put("errDes", "分发现金红包失败,系统异常！");
            _log.info("订单号：" + payOrderId + ",分发现金红包失败,异常信息：" + ex.getStackTrace() + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        }

        return retObj;
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
