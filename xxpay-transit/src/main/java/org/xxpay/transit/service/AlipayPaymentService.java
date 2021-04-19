package org.xxpay.transit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;

import javax.rmi.CORBA.Util;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlipayPaymentService extends BasePayment {

    private final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOnpyVEOvANpRu8lqwhLLxM+oFCDA6DmiReD6v2rKwkSz/s+k88QdN4qaDgdT66PCQGIomPU7N0mN3tJXiWEo8mUxAUD7jyZkDbysyGot9JEbAtDdWrmIe3UE8MvDAW02ahBfm4ZrT8E6mt4Lzqp8aAQ0FqsFJkR1XbgpJ/ITDjtAgMBAAECgYEA15mK21GPbj19CjRX/p791ukkbtEzaPzUY35N/F3mnsheNx+osXRjo9rGkOJDbYudK3K66vV5FSWCgfpP8pjdNM7ycahKwFg952MdtNpty3zpyFDkcWeMjVBSso5wtyqvgILfr4qgS3aHMintLSwSQtUjt5DhulzSyX3Z5YsAI8ECQQD2an3sAsSz4z6RfGlIr5tJc5Mi300btwpROGNmUVxAwbqpqq0UeSOgJEzmoMburS7X/v82Dn4QIfGQc0FZk3mlAkEA8wLOsgUUfsJfp7zcdxwD135EDnYm9jRib2vk9DBGulKV1MkbPMF8Z/3yn9UySH8b8pm3y7o7Ur5iuFb84xtPqQJBANQufp9rAtWjJ40/A6mDDMQCsP+mKE9lHY0ycOT5yeY46vKN9NtcNEEBAPbWGnYKyftTp450jDh4AfnQRMVNJ8ECQQCZ8eRZGCjEqIQKcfVEK2YvpJiehLDn9YWKSlKPcunLbTfnxcLQeU5DXrfOEzQ4gvWEeWba085y+5L0bn7jrFCJAkACF7+rox6W+wkuTfrSp4JAf0brfWwJhTV9QhKAYOzPjHU3xQHPcLq8h9rUitBbo1k8gfP+CW0lve7OZf0ecJl5";
    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    private final String aliPayUrl = "https://openapi.alipay.com/gateway.do";

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    @Autowired
    public PayConfig payConfig;

    private static final MyLog _log = MyLog.getLog(AlipayPaymentService.class);
    public final static String PAY_CHANNEL_ALIPAY_QR_H5 = "alipay_qr_h5";                // 支付宝当面付之H5支付
    public final static String PAY_CHANNEL_ALIPAY_QR_PC = "alipay_qr_pc";                // 支付宝当面付之PC支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_APP = "alipay_coupon_app";            // 支付宝红包无线支付
    public final static String PAY_CHANNEL_ALIPAY_COUNPON_PAGE = "alipay_coupon_page";            // 支付宝红包页面支付

    protected <T> T getObject(JSONObject param, Class<T> clazz) {
        if (param == null) return null;
        return JSON.toJavaObject(param, clazz);
    }


    /**
     * 支付发起
     *
     * @param payOrder
     * @param aliPayConfig
     * @return
     */
    public JSONObject pay(String payOrder, String aliPayConfig) {
        JSONObject retObj;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("开始订单数据RSA解密=》");

            payOrder = payOrder.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");
            byte[] payOrderByte = Base64Utils.decode(payOrder);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payOrderByte, privateKey);
            String payOrderJson = new String(decodedData);

            stringBuffer.append("订单信息解密后Json串=>" + payOrderJson);
            JSONObject jsonObject = JSONObject.parseObject(payOrderJson);
            PayOrder payOrderInfo = getObject(jsonObject, PayOrder.class);
            stringBuffer.append("订单信息Json串反序列化为PayOrder对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);
            String channelId = payOrderInfo.getChannelId();
            String channelName = channelId.substring(channelId.indexOf("_") + 1);
            switch (channelName) {
                case "wap"://wap支付
                    retObj = doAliPayWapReq(payOrderInfo, alipayConfig, stringBuffer);
                    break;
                case "mobile"://app支付
                    retObj = doAliPayMobileReq(payOrderInfo, alipayConfig, stringBuffer);
                    break;
                case "coupon_app"://现金红包
                    retObj = doAliPayTransAppReq(payOrderInfo, alipayConfig, stringBuffer);
                    break;
                case "qr_h5":
                    retObj = doAliPayQrH5Req(payOrderInfo, alipayConfig, stringBuffer);
                    break;
                default:
                    retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的支付宝渠道[channelId=" + channelId + "]");
                    break;
            }

            return retObj;

        } catch (Exception ex) {
            stringBuffer.append("发起支付发生异常：" + ex.getStackTrace() + ex.getMessage());
            retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "跳转发生异常，异常信息：" + ex.getStackTrace() + ex.getMessage());
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }

    /**
     * 支付宝wap支付
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayWapReq(PayOrder payOrder, AlipayConfig alipayConfig, StringBuffer stringBuffer) {
        String logPrefix = "【支付宝WAP支付下单】";
        String payOrderId = payOrder.getPayOrderId();

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;

        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject("消费");
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody("消费");
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
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[" + e.getErrMsg() + "]");
            stringBuffer.append("下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[调取通道异常]");
            stringBuffer.append("下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
        stringBuffer.append("" + logPrefix + "生成跳转路径：payUrl=" + payUrl);
        if (StringUtils.isBlank(payUrl)) {
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "调用支付宝异常!");
            stringBuffer.append("调用支付宝异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
//        rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, null);
        stringBuffer.append("" + logPrefix + "生成请求支付宝数据,req=" + alipay_request.getBizModel());
        stringBuffer.append("###### 商户统一下单处理完成 ######");

        retObj.put("payUrl", payUrl);
        retObj.put("tradeNo", "");
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, "创单成功");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }

    /**
     * 支付宝手机支付 SDK
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayMobileReq(PayOrder payOrder, AlipayConfig alipayConfig, StringBuffer stringBuffer) {
        String logPrefix = "【支付宝APP支付下单】";
        String payOrderId = payOrder.getPayOrderId();
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        JSONObject retObj = buildRetObj();

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
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
            stringBuffer.append("调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        // 封装请求支付信息
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject("消费");
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody("消费");
        model.setProductCode("QUICK_MSECURITY_PAY");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());

        String payUrl = null;
        String tradeNo = null;

        try {
            AlipayTradeAppPayResponse response = client.sdkExecute(alipay_request);
            payUrl = response.getBody();
            tradeNo = response.getTradeNo();
        } catch (AlipayApiException e) {
            _log.error(e, "");
            stringBuffer.append("下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            stringBuffer.append("下单失败[" + e.getMessage() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        if (StringUtils.isBlank(payUrl)) {
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "调用支付宝异常!");
            stringBuffer.append("调用支付宝异常");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        // rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrderId, tradeNo);
        stringBuffer.append("" + logPrefix + "生成请求支付宝数据,payParams=" + payUrl);
        stringBuffer.append("###### 商户统一下单处理完成 ######");

        retObj.put("payUrl", payUrl);
        retObj.put("tradeNo", tradeNo);
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, "创单成功");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }


    /**
     * 支付宝当面付(H5)支付
     * 收银员通过收银台或商户后台调用支付宝接口，可直接打开支付宝app付款。
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayQrH5Req(PayOrder payOrder, AlipayConfig alipayConfig, StringBuffer stringBuffer) {
        String logPrefix = "【支付宝当面付之H5支付下单】";
        String payOrderId = payOrder.getPayOrderId();

        AlipayClient client = null;
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8

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
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        String aliResult;
        String codeUrl = "";
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

            AlipayTradePrecreateResponse alipayresult = client.certificateExecute(alipay_request);
            aliResult = alipayresult.getBody();

            JSONObject aliObj = JSONObject.parseObject(aliResult);
            JSONObject aliResObj = aliObj.getJSONObject("alipay_trade_precreate_response");
            codeUrl = aliResObj.getString("qr_code");
            _log.info("{},响应数据：{}，响应消息：{}", logPrefix, alipayresult.getBody(),alipayresult.getSubMsg());
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[" + e.getErrMsg() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } catch (Exception e) {
            _log.error(e, "");
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[调取通道异常]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }


        _log.info("{}生成支付宝二维码：codeUrl={}", logPrefix, codeUrl);
        String codeImgUrl = alipayConfig.getReqUrl() + "/qrcode_img_get?url=" + codeUrl + "&widht=200&height=200";
        StringBuffer payForm = new StringBuffer();
        String toPayUrl = alipayConfig.getReqUrl() + "/api/alipay/pay_wap.htm";
        payForm.append("<form style=\"display: none\" action=\"" + toPayUrl + "\" method=\"post\">");
        payForm.append("<input name=\"mchOrderNo\" value=\"" + payOrder.getMchOrderNo() + "\" >");
        payForm.append("<input name=\"payOrderId\" value=\"" + payOrder.getPayOrderId() + "\" >");
        payForm.append("<input name=\"amount\" value=\"" + payOrder.getAmount() + "\" >");
        payForm.append("<input name=\"codeUrl\" value=\"" + codeUrl + "\" >");
        payForm.append("<input name=\"codeImgUrl\" value=\"" + codeImgUrl + "\" >");
        payForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
        payForm.append("</form>");
        payForm.append("<script>document.forms[0].submit();</script>");

        stringBuffer.append("" + logPrefix + "生成请求支付宝数据,payParams=" + payForm);
        stringBuffer.append("###### 商户统一下单处理完成 ######");

        retObj.put("payUrl", payForm);
        retObj.put("tradeNo", "");
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, "创单成功");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }


    public JSONObject query(String payOrder, String aliPayConfig) {
        JSONObject retObj;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("开始订单数据RSA解密=》");

            payOrder = payOrder.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");
            byte[] payOrderByte = Base64Utils.decode(payOrder);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payOrderByte, privateKey);
            String payOrderJson = new String(decodedData);

            stringBuffer.append("订单信息解密后Json串=>" + payOrderJson);
            JSONObject jsonObject = JSONObject.parseObject(payOrderJson);
            PayOrder payOrderInfo = getObject(jsonObject, PayOrder.class);
            stringBuffer.append("订单信息Json串反序列化为PayOrder对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);
            return queryExecute(payOrderInfo, alipayConfig);
        } catch (Exception ex) {
            stringBuffer.append("查询发起支付发生异常：" + ex.getStackTrace() + ex.getMessage());
            retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "跳转发生异常，异常信息：" + ex.getStackTrace() + ex.getMessage());
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }


    public JSONObject queryExecute(PayOrder payOrder, AlipayConfig alipayConfig) {
        String logPrefix = "【支付宝订单查询】";
        String payOrderId = payOrder.getPayOrderId();
        String channelOrderNo = payOrder.getChannelOrderNo();
        _log.info("{}开始查询支付宝通道订单,payOrderId={}", logPrefix, payOrderId);
        JSONObject retObj = buildRetObj();
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
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


    /**
     * 单笔转账
     *
     * @param
     * @param
     * @return
     */
    public JSONObject alipaytrans(String payOrderCashCollRecord, String alipayConfigJSon) {

        String logPrefix = "【支付宝转账】";
        String transOrderId = DateUtil.getRevTime();//请求唯一流水号
        JSONObject retObj = buildRetObj();
        StringBuffer stringBuffer = new StringBuffer();
        try {

            stringBuffer.append("开始支付宝转账数据RSA解密=》");

            payOrderCashCollRecord = payOrderCashCollRecord.replace("\\n", "").replace(" ", "+");
            alipayConfigJSon = alipayConfigJSon.replace("\\n", "").replace(" ", "+");
            byte[] payOrderCashCollRecordByte = Base64Utils.decode(payOrderCashCollRecord);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payOrderCashCollRecordByte, privateKey);
            String payOrderJson = new String(decodedData);

            stringBuffer.append("订单信息解密后Json串=>" + payOrderJson);
            JSONObject jsonObject = JSONObject.parseObject(payOrderJson);
            PayOrderCashCollRecord record = getObject(jsonObject, PayOrderCashCollRecord.class);
            stringBuffer.append("订单信息Json串反序列化为PayOrderCashCollRecord对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(alipayConfigJSon);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
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
                retObj.put("msg", "单笔转账封装支付宝参数发生异常:" + ex.getMessage());
                stringBuffer.append("单笔转账封装支付宝参数发生异常:" + ex.getMessage());
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return retObj;
            }

            Random random = new Random();
            model.setOutBizNo(DateUtil.getRevTime() + random.nextInt(10000));
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
                retObj.put("msg", "转账成功");
                retObj.put("channelOrderNo", response.getOrderId());
            } else {
                //出现业务错误
                stringBuffer.append("sub_code:" + response.getSubCode() + ",sub_msg:" + response.getSubMsg());
                retObj.put("msg", response.getSubCode() + response.getSubMsg());
                retObj.put("channelErrCode", response.getSubCode());
            }

            retObj.put("msg", response.getSubMsg() + "|" + response.getMsg());

        } catch (AlipayApiException e) {
            stringBuffer.append("调用支付宝异常:" + e.getMessage());
            _log.error(e, "");
            retObj.put("msg", e.getErrMsg());
            retObj = buildFailRetObj();
        } catch (Exception e) {
            stringBuffer.append("调用支付宝异常:" + e.getMessage());
            e.printStackTrace();
        } finally {
            _log.info(stringBuffer.toString());
        }

        return retObj;
    }

    /**
     * 执行分账
     *
     * @param royalty_parameters
     * @param aliPayConfig
     * @param out_request_no
     * @param trade_no
     * @param passageAccountId
     * @return
     */
    public JSONObject merchantSplit(String royalty_parameters, String aliPayConfig, String out_request_no, String trade_no, String passageAccountId) {
        JSONObject retObj = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("开始分账执行数据RSA解密=》");

            royalty_parameters = royalty_parameters.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");

            byte[] royalty_parametersByte = Base64Utils.decode(royalty_parameters);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(royalty_parametersByte, privateKey);
            String royalty_parametersJson = new String(decodedData);


            stringBuffer.append("分账执行信息解密后Json串=>" + royalty_parametersJson);
            JSONArray jsonArray = JSONArray.parseArray(royalty_parametersJson);

            stringBuffer.append("分账执行Json串反序列化为JSONArray对象");
            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);


            return merchantSplitExecute(jsonArray, alipayConfig, out_request_no, trade_no, passageAccountId);
        } catch (Exception ex) {
            _log.error("资金归集渠道异常：", ex);
            retObj.put("msg", ex.getMessage());
            retObj.put("result", "fail");
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());

        }
    }


    /**
     * 执行分账
     *
     * @param jsonArray
     * @param alipayConfig
     * @param out_request_no
     * @param trade_no
     * @param passageAccountId
     * @return
     */
    public JSONObject merchantSplitExecute(JSONArray jsonArray, AlipayConfig alipayConfig, String out_request_no, String trade_no, String passageAccountId) {

        JSONObject result = new JSONObject();
        result.put("result", "fail");
        try {

            //组装请求报文
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
            certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
            certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
            AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
            AlipayClient alipayClient = null;
            String appCertPublicKeyPath = GetAppCertPublicKey(DateUtil.getRevTime(), alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(DateUtil.getRevTime(), alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(DateUtil.getRevTime(), alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            alipayClient = new DefaultAlipayClient(certAlipayRequest);
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_request_no", "A" + out_request_no);
            bizContent.put("trade_no", trade_no);

            bizContent.put("royalty_parameters", jsonArray);
            request.setBizContent(bizContent.toString());

//            //执行分账前查询账户余额明细,判断余额是否够当前分账的余额
//            JSONObject accountqueryObj = accountquery(alipayConfig);
//            if (accountqueryObj.getString("status") == "0") {
//                result.put("msg", accountqueryObj.getString("msg"));
//                result.put("result", "fail");
//                result.put("closeAccount", passageAccountId);
//                return result;
//            } else {
//                BigDecimal availableAmount = new BigDecimal(accountqueryObj.getString("availableAmount"));
//                BigDecimal fenAmount = new BigDecimal(0);
//                for (Object item : jsonArray) {
//                    JSONObject record = (JSONObject) JSONObject.toJSON(item);
//                    fenAmount.add(new BigDecimal(record.getString("amount")));
//                }
//
//                //如果分账金额大于账户余额
//                if (fenAmount.longValue() > availableAmount.longValue()) {
//                    result.put("msg", "分账失败,失败原因=>支付宝资金账户资产小于当前分账金额，查询返回余额：" + availableAmount);
//                    result.put("result", "fail");
//                    return result;
//                }
//            }

            AlipayTradeOrderSettleResponse response = alipayClient.certificateExecute(request);
            if (response.isSuccess()) {
                _log.error("资金归集接口返回成功, payOrderId={}, code={}, msg={}, subCode={}, subMsg={}",
                        out_request_no, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
                result.put("result", "success");
                result.put("msg", "成功");
                return result;
            }

            _log.error("资金归集接口返回失败, payOrderId={}, code={}, msg={}, subCode={}, subMsg={}",
                    out_request_no, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
            result.put("msg", "资金归集接口返回失败[subCode=" + response.getSubCode() + ", subMsg=" + response.getSubMsg() + "]");

            // 根据错误码判断是否关闭该账号
//            if ("ACQ.TRADE_SETTLE_ERROR".equals(response.getSubCode()) || "isv.insufficient-isv-permissions".equals(response.getSubCode())
//                    || "ACQ.ALLOC_AMOUNT_VALIDATE_ERROR".equals(response.getSubCode())
//                    || "aop.ACQ.SYSTEM_ERROR".equals(response.getSubCode())) {
//                result.put("closeAccount", passageAccountId);
//            }

            result.put("closeAccount", passageAccountId);
            return result;

        } catch (AlipayApiException e) {
            _log.error("资金归集渠道异常：", e);
            result.put("msg", e.getErrMsg());
            result.put("result", "fail");
            return result;
        }
    }

    /**
     * 分账关系绑定
     *
     * @param payCashCollConfig
     * @param aliPayConfig
     * @return
     */
    public JSONObject relationbind(String payCashCollConfig, String aliPayConfig) {
        JSONObject retObj = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("开始分账绑定数据RSA解密=》");

            payCashCollConfig = payCashCollConfig.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");
            byte[] payCashCollConfigJByte = Base64Utils.decode(payCashCollConfig);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payCashCollConfigJByte, privateKey);
            String payCashCollConfigJson = new String(decodedData);

            stringBuffer.append("分账绑定信息解密后Json串=>" + payCashCollConfigJson);
            JSONObject jsonObject = JSONObject.parseObject(payCashCollConfigJson);
            PayCashCollConfig payCashCollConfigInfo = getObject(jsonObject, PayCashCollConfig.class);
            stringBuffer.append("分账绑定Json串反序列化为PayCashCollConfig对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);

            return relationbindExecute(payCashCollConfigInfo, alipayConfig);
        } catch (Exception ex) {
            retObj.put("errDes", "AlipayCashCollService=》绑定商家分账关系发生异常");
            stringBuffer.append("AlipayCashCollService=》绑定商家分账关系发生异常[" + ex.getMessage() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());

        }
    }


    /**
     * 分账绑定执行
     *
     * @param payProduct
     * @param alipayConfig
     * @return
     */
    public JSONObject relationbindExecute(PayCashCollConfig payProduct, AlipayConfig alipayConfig) {
        JSONObject result = new JSONObject();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("商家分账关系绑定,子账户ID:" + payProduct.getBelongPayAccountId() + "，分配支付账号：" + payProduct.getTransInUserId() + "，ID：" + payProduct.getTransInUserName());
        try {

            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
            certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
            certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8

            AlipayTradeRoyaltyRelationBindRequest alipay_request = new AlipayTradeRoyaltyRelationBindRequest();
            AlipayTradeRoyaltyRelationBindModel model = new AlipayTradeRoyaltyRelationBindModel();
            List<RoyaltyEntity> receiverList = new LinkedList<>();
            model.setOutRequestNo(DateUtil.getRevTime());
            RoyaltyEntity royaltyEntity = new RoyaltyEntity();
            Map map = new HashMap();
            royaltyEntity.setAccount(payProduct.getTransInUserId());
            royaltyEntity.setType("userId");
            royaltyEntity.setMemo("分账给" + payProduct.getTransInUserName());
            royaltyEntity.setName(payProduct.getTransInUserName());
            receiverList.add(royaltyEntity);
            model.setReceiverList(receiverList);
            alipay_request.setBizModel(model);
            JSONObject jsonObject = new JSONObject();
            AlipayClient alipayClient = null;
            try {
                String appCertPublicKeyPath = GetAppCertPublicKey(DateUtil.getRevTime(), alipayConfig.getAppId());
                String alipayCertPublicKeyPath = GetAlipayCertPublicKey(DateUtil.getRevTime(), alipayConfig.getAppId());
                String alipayRootCertPath = GetAlipayRootCert(DateUtil.getRevTime(), alipayConfig.getAppId());
                certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
                certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
                certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
                certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
                alipayClient = new DefaultAlipayClient(certAlipayRequest);
                AlipayTradeRoyaltyRelationBindResponse response = alipayClient.certificateExecute(alipay_request);
                if (response.isSuccess()) {
                    stringBuffer.append("=》商家分账关系绑定成功");
                    result.put("errDes", "商家分账关系绑定成功");
                    result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                    return result;
                }
                stringBuffer.append("商家分账关系绑定失败,失败原因：" + response.getSubMsg());
                result.put("errDes", "商家分账关系绑定失败,失败原因：" + response.getSubMsg());
                result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return result;

            } catch (AlipayApiException e) {
                _log.error(e, "");
                result.put("errDes", "商家分账关系绑定异常[" + e.getErrMsg() + "]");
                stringBuffer.append("商家分账关系绑定异常[" + e.getErrMsg() + "]");
                result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return result;
            } catch (Exception ex) {
                _log.error(ex, "");
                result.put("errDes", "商家分账关系绑定异常");
                stringBuffer.append("商家分账关系绑定异常[" + ex.getMessage() + "]");
                result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return result;
            }

        } catch (Exception ex) {
            result.put("errDes", "AlipayCashCollService=》绑定商家分账关系发生异常");
            stringBuffer.append("AlipayCashCollService=》绑定商家分账关系发生异常[" + ex.getMessage() + "]");
            result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return result;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }


    public JSONObject querymerchantSplit(String payOrder, String aliPayConfig) {
        JSONObject retObj;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("分账订单信息查询");
        try {
            stringBuffer.append("开始订单数据RSA解密=》");

            payOrder = payOrder.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");
            byte[] payOrderByte = Base64Utils.decode(payOrder);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payOrderByte, privateKey);
            String payOrderJson = new String(decodedData);

            stringBuffer.append("订单信息解密后Json串=>" + payOrderJson);
            JSONObject jsonObject = JSONObject.parseObject(payOrderJson);
            PayOrder payOrderInfo = getObject(jsonObject, PayOrder.class);
            stringBuffer.append("订单信息Json串反序列化为PayOrder对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);

            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);
            return querymerchantSplitExecute(payOrderInfo, alipayConfig);
        } catch (Exception ex) {
            stringBuffer.append("查询发起支付发生异常：" + ex.getStackTrace() + ex.getMessage());
            retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "跳转发生异常，异常信息：" + ex.getStackTrace() + ex.getMessage());
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }


    public JSONObject querymerchantSplitExecute(PayOrder payOrderInfo, AlipayConfig alipayConfig) {

        String logPrefix = "【分账订单信息查询】";
        _log.info("{}开始查询支付宝通道订单,payOrderId={}", logPrefix, payOrderInfo.getPayOrderId());
        JSONObject retObj = buildRetObj();
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        try {
            String appCertPublicKeyPath = GetAppCertPublicKey(payOrderInfo.getPayOrderId(), alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(payOrderInfo.getPayOrderId(), alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(payOrderInfo.getPayOrderId(), alipayConfig.getAppId());
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
        alipay_request.setBizContent("{" +
                "\"out_trade_no\":\"" + payOrderInfo.getPayOrderId() + "\"," +
                "\"trade_no\":\"" + payOrderInfo.getChannelOrderNo() + "\"," +
                "\"org_pid\":\"" + alipayConfig.getPid() + "\"," +
                "      \"query_options\":[" +
                "        \"TRADE_SETTLE_INFO\"" +
                "      ]" +
                "  }");

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


            msg = alipay_response.getSubMsg();
            _log.info("channelOrderNo={},payOrderId={}分账状态查询响应:{}。执行状态:{}", payOrderInfo.getChannelOrderNo(), payOrderInfo.getPayOrderId(), alipay_response.getBody(), alipay_response.isSuccess());

            if (!alipay_response.isSuccess()) {
                retObj.put("status", "fail");
                retObj.put("msg", msg);
                return retObj;
            }

            TradeSettleInfo tradeSettleInfo = alipay_response.getTradeSettleInfo();
            if (tradeSettleInfo == null) {
                retObj.put("status", "3");   //延迟结算
                retObj.put("msg", "延迟结算");
                return retObj;
            }

            BigDecimal amount = new BigDecimal(0);
            for (TradeSettleDetail item : tradeSettleInfo.getTradeSettleDetailList()) {
                amount.add(new BigDecimal(item.getAmount()));
            }

            retObj.put("amount", String.valueOf(amount));
            _log.info("{}payOrderId={}返回结果:{}", logPrefix, payOrderInfo.getPayOrderId(), result);

        } catch (AlipayApiException e) {
            _log.error(e, "");
        }

        retObj.put("channelOrderNo", payOrderInfo.getChannelOrderNo());
        retObj.put("status", "1");    // 支付中
        if ("TRADE_SUCCESS".equals(result)) {
            retObj.put("status", "2");    // 成功
            retObj.put("msg", "支付成功");
        } else if ("WAIT_BUYER_PAY".equals(result)) {
            retObj.put("status", "1");    // 支付中
        } else {
            retObj.put("status", "1");    // 支付中
            retObj.put("msg", msg);    // 支付中
        }

        return retObj;
    }


    /**
     * 支付宝现金红包无线支付 now
     *
     * @param payOrder
     * @return
     */
    public JSONObject doAliPayTransAppReq(PayOrder payOrder, AlipayConfig alipayConfig, StringBuffer stringBuffer) {
        String logPrefix = "【支付宝红包无线支付下单】";
        JSONObject retObj = buildRetObj();
        String payOrderId = payOrder.getPayOrderId();
        AlipayFundTransAppPayRequest alipay_request = new AlipayFundTransAppPayRequest();
        AlipayClient client = null;
        try {

            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
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
        String pid = payOrder.getParam2();//这里这个ID，传过来吧
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

        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 支付宝红包无线支付下单处理完成 ######");

        CacheUtil.put(payOrder.getMchOrderNo(), payUrl, 300);//SDK支付字符串

        payUrl = alipayConfig.getReqUrl() + "/index?mchOrderId=" + payOrder.getMchOrderNo() + "&amount=" + AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()));

        //中转服务器地址
        String goPayUrl = alipayConfig.getReqUrl() + "/alipay?orderId=" + payOrder.getMchOrderNo();

        //阿里内置浏览器访问地址
        String goAliPayUrl = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + alipayConfig.getReqUrl() + "/alipay?orderId=" + payOrder.getMchOrderNo();


        CacheUtil.put("AliPay" + payOrder.getMchOrderNo(), goAliPayUrl, 300);//本地缓存支付宝支付链接，缓存时间5分钟

        retObj.put("payUrl", payUrl);
        retObj.put("tradeNo", channelOrderNo);
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, "创单成功");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }


    /**
     * 支付发起
     *
     * @param payOrder
     * @param aliPayConfig
     * @return
     */
    public JSONObject transunitransfer(String payOrder, String aliPayConfig, String payCashCollConfig) {
        JSONObject retObj;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("开始红包分发数据RSA解密=》");

            payOrder = payOrder.replace("\\n", "").replace(" ", "+");
            aliPayConfig = aliPayConfig.replace("\\n", "").replace(" ", "+");
            payCashCollConfig = payCashCollConfig.replace("\\n", "").replace(" ", "+");

            byte[] payOrderByte = Base64Utils.decode(payOrder);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(payOrderByte, privateKey);
            String payOrderJson = new String(decodedData);

            stringBuffer.append("红包分发信息解密后Json串=>" + payOrderJson);
            JSONObject jsonObject = JSONObject.parseObject(payOrderJson);
            PayOrder payOrderInfo = getObject(jsonObject, PayOrder.class);
            stringBuffer.append("红包信息Json串反序列化为PayOrder对象");

            //Ali参数配置信息
            stringBuffer.append("Ali参数配置信息RSA解密=》");
            byte[] aliPayConfigByte = Base64Utils.decode(aliPayConfig);
            byte[] aliPayConfigDeByte = RSAUtils.decryptByPrivateKey(aliPayConfigByte, privateKey);


            String aliPayConfigJson = new String(aliPayConfigDeByte);
            stringBuffer.append("Ali参数配置信息RSA解密后Json=》" + aliPayConfigJson);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);


            stringBuffer.append("payCashCollConfig参数配置信息RSA解密=》");
            byte[] payCashCollConfigByte = Base64Utils.decode(payCashCollConfig);
            byte[] decodedpayCashCollConfig = RSAUtils.decryptByPrivateKey(payCashCollConfigByte, privateKey);
            String payCashCollConfigJson = new String(decodedpayCashCollConfig);
            stringBuffer.append("payCashCollConfig参数配置信息RSA解密后Json=》" + payCashCollConfigJson);
            JSONObject payCashCollConfigObj = JSONObject.parseObject(payCashCollConfigJson);
            PayCashCollConfig payCashCollConfigInfo = getObject(payCashCollConfigObj, PayCashCollConfig.class);
            JSONObject res = this.transunitransferExecute(payOrderInfo, alipayConfig, payCashCollConfigInfo, stringBuffer);
            return res;

        } catch (Exception ex) {
            ex.printStackTrace();
            stringBuffer.append("红包分发发生异常：" + ex.getStackTrace() + ex.getMessage());
            retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "红包分发,发生异常，异常信息：" + ex.getStackTrace() + ex.getMessage());
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }


    /**
     * 现金红包分发
     *
     * @param payOrder
     * @return
     */

    public JSONObject transunitransferExecute(PayOrder payOrder, AlipayConfig alipayConfig, PayCashCollConfig payCashCollConfig, StringBuffer stringBuffer) {
        JSONObject retObj = buildRetObj();
        String logPrefix = "【支付宝红包无线支付红包领取】";
        String payOrderId = payOrder.getPayOrderId();
        stringBuffer.append("订单号:" + payOrderId + "," + logPrefix);
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
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
            stringBuffer.append("调用支付宝异常:" + ex.getMessage());
            retObj.put("errDes", "调用支付宝异常:" + ex.getMessage());
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }

        if (payCashCollConfig == null) {
            stringBuffer.append("分发现金红包失败,未获取到收红包的账号信息");
            retObj.put("errDes", "分发现金红包失败,未获取到收红包的账号信息");
            _log.info("订单号：" + payOrderId + ",分发现金红包失败：未获取到收红包的账号信息");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        }

        //获取收红包的支付宝账号信息 2088232513401452 邱家雄
        AlipayFundTransUniTransferRequest alipay_request = new AlipayFundTransUniTransferRequest();
        alipay_request.setBizContent("{" +
                "\"out_biz_no\":\"" + payOrderId + "\"," +
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

        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getAlipayAccount());
        // 设置同步跳转地址
        try {

            AlipayFundTransUniTransferResponse response = client.certificateExecute(alipay_request);
            if (response.isSuccess()) {
                retObj.put("errDes", "分发现金红包成功!");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                retObj.put("errDes", "分发现金红包失败,领取人:" + payCashCollConfig.getTransInUserId() + "。失败原因：" + response.getSubMsg() + "");
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


    public JSONObject accountquery(AlipayConfig alipayConfig) {
        StringBuffer stringBuffer = new StringBuffer();
        JSONObject retObj = buildRetObj();
        String logPrefix = "【支付宝资金账户资产查询接口】";
        stringBuffer.append(logPrefix);
        stringBuffer.append("商户号:" + alipayConfig.getPid());
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        AlipayFundAccountQueryRequest request = new AlipayFundAccountQueryRequest();
        request.setBizContent("{" +
                "\"alipay_user_id\":\"" + alipayConfig.getPid() + "\"," +
                "\"account_type\":\"ACCTRANS_ACCOUNT\"," +
                "  }");
        try {

            String appCertPublicKeyPath = GetAppCertPublicKey("", alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey("", alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert("", alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);
            AlipayFundAccountQueryResponse response = client.certificateExecute(request);
            stringBuffer.append("支付宝资金账户资产查询接口响应信息：" + response.getBody());
            if (response.isSuccess()) {
                retObj.put("status", "1");
                retObj.put("availableAmount", new BigDecimal(response.getAvailableAmount()).add(new BigDecimal(10)));
                return retObj;
            } else {
                retObj.put("status", "0");
                retObj.put("msg", MessageFormat.format("账户余额查询失败=>响应码：{0}，响应信息:{1}", response.getCode() + response.getSubMsg()));
                return retObj;
            }

        } catch (AlipayApiException e) {
            _log.info(e.getStackTrace() + e.getMessage());
            retObj.put("status", "0");
            retObj.put("msg", MessageFormat.format("账户余额查询发生异常=>异常信息：{0}", e.getStackTrace() + e.getMessage()));
            return retObj;
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info(ex.getStackTrace() + ex.getMessage());
            retObj.put("status", "0");
            retObj.put("msg", MessageFormat.format("账户余额查询发生异常=>异常信息：{0}", ex.getStackTrace() + ex.getMessage()));
            return retObj;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }
}
