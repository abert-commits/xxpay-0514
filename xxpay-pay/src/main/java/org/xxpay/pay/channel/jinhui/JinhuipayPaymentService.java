package org.xxpay.pay.channel.jinhui;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
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
import java.util.SortedMap;
import java.util.TreeMap;

import static org.xxpay.core.common.util.PayDigestUtil.md5;

@Service
public class JinhuipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JinhuipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JINHUIAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doJinHuiPayReq(payOrder, "ALISCAN");//ALISCAN 支付宝扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJinHuiPayReq(payOrder, "ALIH5"); //ALIH5 支付宝 H5 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJinHuiPayReq(payOrder, "ALISDK"); //ALIH5 支付宝 sdk 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doJinHuiPayReq(payOrder, "WXSCAN"); //WXSCAN 微信扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doJinHuiPayReq(payOrder, "WXH5"); //微信 H5 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_QQ_QR:
                retObj = doJinHuiPayReq(payOrder, "QQSCAN"); //QQ扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_QQ_WAP:
                retObj = doJinHuiPayReq(payOrder, "QQH5"); //QQH5 支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJinHuiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【嘉城支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();

            map.put("mch_id", channelPayConfig.getMchId());//间连号
            map.put("trade_type", channel);
            map.put("nonce", RandomStringUtils.randomAlphanumeric(31));//随机数
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//时间搓 10
            map.put("subject", "韩信订单");// 订单标题
            map.put("out_trade_no", payOrder.getPayOrderId()); // 商户订单号
            map.put("total_fee", String.valueOf(payOrder.getAmount()));// 单位 分
            map.put("spbill_create_ip", "87.200.185.201"); // 终端ip
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); // 异步返回地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/unifiedorder", map.toJSONString());
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);

            String resultCode = resObj.getString("result_code");
            String retMsg = resObj.getString("message");
            if ("SUCCESS".equals(resultCode)) {
                // 第一步拼接字符串：
                SortedMap mapJM = new TreeMap();
                mapJM.put("mch_id", resObj.getString("mch_id"));//间连号
                mapJM.put("trade_type", resObj.getString("trade_type"));//交易类型
                mapJM.put("nonce", resObj.getString("nonce"));//随机字符串
                mapJM.put("result_code", resObj.getString("result_code"));//结果状态
                mapJM.put("pay_url", resObj.getString("pay_url"));//支付地址
                mapJM.put("message", resObj.getString("message"));//结果描述

                // String stringMap = XXPayUtil.mapToString(mapJM).replace(">", "");
                // md5 加密
                String md5 = PayDigestUtil.getSign(mapJM, channelPayConfig.getmD5Key());
                // RSA2 验签
                // boolean md5Sign=PayDigestUtil.verify(md5,PayDigestUtil.getPublicKey(channelPayConfig.getRsaPublicKey()),resObj.getString("sign"),"utf8");
                if (md5.equals(resObj.getString("sign"))) {
                    String payJumpUrl = resObj.getString("pay_url");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payOrder.getChannelId().contains("SDK")) {
                        //SDK跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payOrder.getChannelId().contains("QR")) {
                        //二维码
                        payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                    } else {
                        //表单跳转
                        if (payJumpUrl.contains("form")) {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                        } else {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                        }
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
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
        String logPrefix = "【嘉城支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());//商户号
            map.put("nonce", RandomStringUtils.randomAlphanumeric(31));//随机字符串
            map.put("out_trade_no", payOrder.getPayOrderId());//商户订单
            map.put("platform_trade_ no", payOrder.getChannelOrderNo());//平台交易单号
            map.put("sign_type", "MD5 ");//签名类型

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            if (retCode.equals("100")) {
                // 订单成功
                BigDecimal amount = resObj.getBigDecimal("amount");
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
