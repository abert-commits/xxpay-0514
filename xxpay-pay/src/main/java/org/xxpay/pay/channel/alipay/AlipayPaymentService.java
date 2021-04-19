package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.*;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.util.privilegedactions.GetResource;
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
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.CashCollInterface;
import org.xxpay.pay.mq.BaseNotify4CashColl;
import org.xxpay.pay.mq.BaseNotify4MchPay;
import org.xxpay.pay.util.SpringUtil;
import org.xxpay.pay.util.Util;

import java.net.URLDecoder;
import java.util.*;

import javafx.util.Pair;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
@Service
public class AlipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(AlipayPaymentService.class);
    public final static String PAY_CHANNEL_ALIPAY_QR_H5 = "alipay_qr_h5";                // 支付宝当面付之H5支付
    public final static String PAY_CHANNEL_ALIPAY_QR_PC = "alipay_qr_pc";                // 支付宝当面付之PC支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_APP = "alipay_coupon_app";            // 支付宝红包无线支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_PAGE = "alipay_coupon_page";            // 支付宝红包页面支付

    private List<String> listProducts = new LinkedList<String>() {
    };

    @Autowired
    public BaseNotify4MchPay baseNotify4MchPay;

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    private String BuidProducts() {
        listProducts.add("电子产品");
        listProducts.add("服装产品");
        listProducts.add("数码产品");
        listProducts.add("生活用品");
        Random random = new Random();
        int n = random.nextInt(listProducts.size());
        return listProducts.get(n);
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        String channelId = payOrder.getChannelId();
        JSONObject retObj;
        switch (channelId) {
            case PayConstant.PAY_CHANNEL_ALIPAY_MOBILE:
                retObj = doAliPayMobileReq(payOrder);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_PC:
                retObj = doAliPayPcReq(payOrder);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_WAP:
                retObj = doAliPayWapReq(payOrder);
                break;
            case PayConstant.PAY_CHANNEL_ALIPAY_QR:
                retObj = doAliPayQrReq(payOrder);
                break;
            case PAY_CHANNEL_ALIPAY_QR_H5:
                retObj = doAliPayQrH5Req(payOrder, "wap");
                break;
            case PAY_CHANNEL_ALIPAY_QR_PC:
                retObj = doAliPayQrPcReq(payOrder, "pc");
                break;
            case PAY_CHANNEL_ALIPAY_COUNPON_APP:
                retObj = doAliPayTransAppReq(payOrder);//支付宝现金红包无线支付
//                retObj = doAliPayTransAppReqNow(payOrder);
                break;
            case PAY_CHANNEL_ALIPAY_COUNPON_PAGE:
                retObj = doAliPayCouponPageReq(payOrder);
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的支付宝渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    @Autowired
    private BaseNotify4CashColl baseNotify4CashColl;

    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【支付宝订单查询】";
        String payOrderId = payOrder.getPayOrderId();
        String channelOrderNo = payOrder.getChannelOrderNo();
        _log.info("{}开始查询支付宝通道订单,payOrderId={}", logPrefix, payOrderId);
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        JSONObject retObj = buildRetObj();
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl("https://openapi.alipay.com/gateway.do");  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        try {
            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception ex) {
            retObj.put("errDes", "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        AlipayTradeQueryRequest alipay_request = new AlipayTradeQueryRequest();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payOrderId);
        model.setTradeNo(channelOrderNo);
        alipay_request.setBizModel(model);
        AlipayTradeQueryResponse alipay_response;
        String result = "";
        String msg = "支付中";
        try {
            alipay_response = client.certificateExecute(alipay_request);
            // 交易状态：
            // WAIT_BUYER_PAY（交易创建，等待买家付款）、
            // TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
            // TRADE_SUCCESS（交易支付成功）、
            // TRADE_FINISHED（交易结束，不可退款）
            result = alipay_response.getTradeStatus();
            channelOrderNo = alipay_response.getTradeNo();
            msg = alipay_response.getSubMsg();
            _log.info("{}payOrderId={}返回结果:{}", logPrefix, payOrderId, result);

        } catch (AlipayApiException e) {
            _log.error(e, "");
        }

        retObj.put("channelOrderNo", channelOrderNo);
        retObj.put("status", 1);    // 支付中
        if ("TRADE_SUCCESS".equals(result)) {
            retObj.put("status", 2);    // 成功
            retObj.put("msg", "支付成功");
        } else if ("WAIT_BUYER_PAY".equals(result)) {
            retObj.put("status", 1);    // 支付中
            retObj.put("msg", "未支付");
        } else {
            retObj.put("status", 1);    // 支付中
            retObj.put("msg", msg);    // 支付中
        }

        return retObj;
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

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;

        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(BuidProducts());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(BuidProducts());
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
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        // 设置同步跳转地址
//        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        String payUrl = null;
        JSONObject retObj = buildRetObj();
        try {

            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);

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
        String logPrefix = "【支付宝APP支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        JSONObject retObj = buildRetObj();

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        try {
            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception ex) {
            retObj.put("errDes", "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        // 封装请求支付信息
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(BuidProducts());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(BuidProducts());
        model.setProductCode("QUICK_MSECURITY_PAY");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        // 设置同步跳转地址
        String payUrl = null;
        String tradeNo = null;

        try {
            AlipayTradeAppPayResponse response = client.sdkExecute(alipay_request);
            payUrl = response.getBody();
            tradeNo = response.getTradeNo();

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

        if (StringUtils.isBlank(payUrl)) {
            retObj.put("errDes", "调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, tradeNo);
        _log.info("{}生成请求支付宝数据,payParams={}", logPrefix, payUrl);
        _log.info("###### 商户统一下单处理完成 ######");
        JSONObject payParams = new JSONObject();
        payParams.put("payJumpUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
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
            String body = client.sdkExecute(alipay_request).getBody();
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
        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
        retObj.put("payParams", payParams);
        return retObj;
    }


    /**
     * 支付宝现金红包无线支付 now
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayTransAppReq(PayOrder payOrder) {
        String logPrefix = "【支付宝红包无线支付下单】";
        JSONObject retObj = buildRetObj();
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayFundTransAppPayRequest alipay_request = new AlipayFundTransAppPayRequest();
        AlipayClient client = null;
        try {
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
            certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
            certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8

            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());

            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）

            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception ex) {
            retObj.put("errDes", "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }


        Random random = new Random();
        String pid = "2088902020727558";
        alipay_request.setBizContent("{" +
                "\"out_biz_no\":\"" + payOrderId + "\"," +
                "\"trans_amount\":" + AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()) + "," +
                "\"product_code\":\"STD_RED_PACKET\"," +
                "\"biz_scene\":\"PERSONAL_PAY\"," +
                "\"remark\":\"拼手气红包\"," +
                "\"order_title\":\"手气红包\"," +
                "\"time_expire\":\"" + DateUtil.date2Str(DateUtil.minusDateByMinute(new Date(), -5), DateUtil.FORMAT_YYYY_MM_DD_HH_MM) + "" +
                "" +
                "" +
                "\"," +
                "\"refund_time_expire\":\"" + DateUtil.date2Str(DateUtil.minusDateByMinute(new Date(), -10), DateUtil.FORMAT_YYYY_MM_DD_HH_MM) + "\"," +
                "\"business_params\":\"{\\\"sub_biz_scene\\\":\\\"REDPACKET\\\",\\\"payer_binded_alipay_uid\\\":\\\"" + pid + "\\\"}\"" +
                "  }");

        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        // 设置同步跳转地址
        String payUrl = null;
        String channelOrderNo = "";
        try {

            AlipayFundTransAppPayResponse response = client.sdkExecute(alipay_request);
            payUrl = response.getBody();
            channelOrderNo = response.getOrderId();

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
        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, channelOrderNo);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");

        retObj.put("payOrderId", payOrderId);
        JSONObject payParams = new JSONObject();
        payParams.put("payJumpUrl", payUrl);
        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
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
        JSONObject retObj = buildRetObj();
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        AlipayClient client = null;
        AlipayFundCouponOrderPagePayRequest alipay_request = new AlipayFundCouponOrderPagePayRequest();
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
        String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
        String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());
        certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
        certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
        certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
        certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
        String payUrl = null;
        try {
            client = new DefaultAlipayClient(certAlipayRequest);
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

        JSONObject payParams = new JSONObject();
        payParams.put("payJumpUrl", payUrl);
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

    public JSONObject transunitransfer(PayOrder payOrder) {
        JSONObject retObj = buildRetObj();
        String logPrefix = "【支付宝红包无线支付单笔转账】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl("https://openapi.alipay.com/gateway.do");  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        try {

            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());

            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception ex) {
            retObj.put("errDes", "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        PayCashCollConfig payCashCollConfig = QueryPayCashCollConfig(payOrder.getPassageAccountId().toString(), MchConstant.PUB_YES);
        if (payCashCollConfig == null) {
            retObj.put("errDes", "分发现金红包失败,未获取到收红包的账号信息");
            _log.info("订单号：" + payOrderId + ",分发现金红包失败：未获取到收红包的账号信息");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        }

//      rpcCommonService.rpcPayOrderCashCollRecordService.add()
        //获取收红包的支付宝账号信息 2088232513401452 邱家雄
        String requestId = String.valueOf(System.currentTimeMillis());
        payOrder.setAmount(CashUnpacking(payOrder));//现金红包拆包
        AlipayFundTransUniTransferRequest alipay_request = new AlipayFundTransUniTransferRequest();
        alipay_request.setBizContent("{" +
                "\"out_biz_no\":\"" + requestId + "\"," +
                "\"trans_amount\":" + AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()) + "," +
                "\"product_code\":\"STD_RED_PACKET\"," +
                "\"biz_scene\":\"PERSONAL_COLLECTION\"," +
                "\"order_title\":\"人情往来\"," +
                "\"original_order_id\":\"" + payOrder.getChannelOrderNo() + "\"," +
                "\"payee_info\":{" +
                "\"identity\":\"" + payCashCollConfig.getTransInUserId() + "\"," +
                "\"identity_type\":\"ALIPAY_USER_ID\"," +
                "\"name\":\"" + payCashCollConfig.getTransInUserName() + "\"" +
                "    }," +
                "\"remark\":\"红包领取\"," +
                "\"business_params\":\"{\\\"sub_biz_scene\\\":\\\"REDPACKET\\\"}\"" +
                "  }");

        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        // 设置同步跳转地址
        alipay_request.setReturnUrl(payConfig.getReturnUrl(getChannelName()));
        try {

            AlipayFundTransUniTransferResponse response = client.certificateExecute(alipay_request);
            //插入资金归集
            PayOrderCashCollRecord record = new PayOrderCashCollRecord();
            record.setPayOrderId(payOrderId);
            record.setTransInUserId(payCashCollConfig.getTransInUserId());
            record.setChannelOrderNo(payOrder.getChannelOrderNo());
            record.setRequestNo(requestId);
            record.setTransInPercentage(payCashCollConfig.getTransInPercentage());
            record.setTransInAmount(payOrder.getAmount());
            record.setTransInUserAccount(payCashCollConfig.getTransInUserAccount());
            record.setCreateTime(new Date());
            record.setRemark(response.isSuccess() ? "现金红包领取成功" : response.getSubMsg());
            record.setType(2);
            record.setTransInUserName(payCashCollConfig.getTransInUserName());
            record.setStatus(response.isSuccess() ? (byte) 1 : (byte) 0);
            record.setPassageAccountId(payOrder.getPassageAccountId().toString());
            rpcCommonService.rpcPayOrderCashCollRecordService.add(record);
            if (response.isSuccess()) {
                retObj.put("errDes", "分发现金红包成功!");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                retObj.put("errDes", "分发现金红包失败!");
                _log.info("订单号：" + payOrderId + ",分发现金红包失败：" + response.getSubMsg());
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            }

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

        return retObj;
    }


    /**
     * 支付宝单笔转账到支付宝
     *
     * @param record
     * @param account
     * @return
     */
    public JSONObject alipaytrans(PayOrderCashCollRecord record, PayPassageAccount account) {
        String logPrefix = "【支付宝转账】";
        String transOrderId = record.getPayOrderId();
        JSONObject retObj = buildRetObj();
        try {
            AlipayConfig alipayConfig = new AlipayConfig(account.getParam());
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
            certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
            certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8

            AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
            AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
            AlipayClient alipayClient = null;
            try {

                String appCertPublicKeyPath = GetAppCertPublicKey(record.getPayOrderId(), alipayConfig.getAppId());
                String alipayCertPublicKeyPath = GetAlipayCertPublicKey(record.getPayOrderId(), alipayConfig.getAppId());
                String alipayRootCertPath = GetAlipayRootCert(record.getPayOrderId(), alipayConfig.getAppId());
                certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
                certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
                certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
                certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
                alipayClient = new DefaultAlipayClient(certAlipayRequest);
            } catch (Exception ex) {
                retObj.put("msg", "现金红包封装支付宝参数发生异常:" + ex.getMessage());
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return retObj;
            }

            model.setOutBizNo(DateUtil.getRevTime());
            model.setPayeeType("ALIPAY_LOGONID");                            // 收款方账户类型
            model.setPayeeAccount(record.getTransInUserAccount());              // 收款方账户
            model.setAmount(AmountUtil.convertCent2Dollar(record.getTransInAmount().toString()));//分转元
            model.setPayerShowName("支付转账");
            model.setPayeeRealName(record.getTransInUserName());
            model.setRemark(record.getRemark());
            request.setBizModel(model);
            retObj.put("transOrderId", transOrderId);
            retObj.put("state", "fail");

            AlipayFundTransToaccountTransferResponse response = alipayClient.certificateExecute(request);
            if (response.isSuccess()) {
                retObj.put("state", "success");
                retObj.put("channelOrderNo", response.getOrderId());
            } else {
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                retObj.put("channelErrCode", response.getSubCode());
            }

            retObj.put("msg", response.getSubMsg());

        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("msg", e.getErrMsg());
            retObj = buildFailRetObj();
        }

        return retObj;
    }

    /**
     * 支付宝现金红包订单查询
     *
     * @param payOrder
     * @return
     */

    public JSONObject transcommonquery(PayOrder payOrder) {
        JSONObject retObj = buildRetObj();
        String logPrefix = "【支付宝现金红包订单查询】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getPayParam(payOrder));
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(alipayConfig.getReqUrl());  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        try {

            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderId, alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception ex) {
            retObj.put("msg", "现金红包封装支付宝参数发生异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        AlipayFundTransCommonQueryRequest alipay_request = new AlipayFundTransCommonQueryRequest();
        alipay_request.setBizContent("{" +
                "\"product_code\":\"STD_RED_PACKET\"," +
                "\"biz_scene\":\"PERSONAL_PAY\"," +
                "\"out_biz_no\":\"" + payOrderId + "\"," +
                "\"order_id\":\"\"," +
                "\"pay_fund_order_id\":\"\"" +
                "  }");
        try {

            //查询现金红包订单状态
            AlipayFundTransCommonQueryResponse response = client.certificateExecute(alipay_request);
            if (response.isSuccess() && response.getStatus().toUpperCase().equals("SUCCESS")) {
                //查询成功，且订单状态为转账成功。（这里指的转账成功是指发红包的人支付成功）
                retObj.put("msg", "现金红包转账成功");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                retObj.put("msg", "现金红包等待支付或订单超时关闭,信息：" + response.getSubMsg());
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            }

            return retObj;

        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("msg", "现金红包订单查询失败：[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put("msg", "现金红包订单查询失败：[" + e.getMessage() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }


}
