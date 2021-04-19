package org.xxpay.pay.channel.kaixin;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.sandpay.sdk.AESTool;

import java.util.Map;
import java.util.TreeMap;

@Service
public class KaixinpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(KaixinpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_KAIXINPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK: //支付宝 原生sdk
                retObj = doKaixinPayReq(payOrder, "H51");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doKaixinPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【开心支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("service", "pay.alipay");  //服务类型，固定值：pay.alipay
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户系统内部的订单号,32个字符内、可包含字母
            map.put("trade_time", DateUtil.getSeqString()); //订单交易时间，YYYYmmDDHHMiSS，如：20171001122345
            map.put("subject", "开心"); //商品标题
            map.put("body", "开心"); //商品描述
            map.put("total_fee", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //总金额，以元为单位，不允许包括小数点之外的字符。如一分钱为0.01
            map.put("spbill_create_ip", "127.0.0.1"); //用户支付提交的ip地址
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //接收支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); //接收支付界面通知返回地址， url必须为直接可访问的url。
            map.put("sign_type", "MD5"); //签名类型：MD5
            map.put("trade_type", channel); //取值如下：H5

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            map.put("content_type", "json"); //取值如下：H5

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/payapi/gateway.do", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String return_code = resObj.getString("return_code");
            String err_msg = resObj.getString("err_msg");
            if ("success".equals(return_code)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {
                    String payJumpUrl = resObj.getString("mweb_url");
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
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("out_trade_no"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("下单失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[延签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                payInfo.put("errDes", "下单失败[" + err_msg + "]");
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
        String logPrefix = "【开心支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            Map map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("service", "trade.query");  //服务类型，固定值：trade.query
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户系统内部的订单号,32个字符内、可包含字母
            map.put("trade_time", DateUtil.getSeqString()); //订单交易时间，YYYYmmDDHHMiSS，如：20171001122345
            map.put("spbill_create_ip", "127.0.0.1"); //用户支付提交的ip地址
            map.put("sign_type", "MD5"); //签名类型：MD5

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/payapi/gateway.do", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String return_code = resObj.getString("return_code");
            String err_msg = resObj.getString("err_msg");
            if ("success".equals(return_code)) {
                //支付成功
                String trade_status = resObj.getString("trade_status");
                if (trade_status.equals("1")) {
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + trade_status+ ",订单状态:" + GetStatusMsg(trade_status) + "");
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", err_msg);
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "未支付";
            case "1":
                return "支付成功";
            case "2":
                return "异常 ";
            case "99":
                return "无此订单 ";
            default:
                return "交易失败";
        }
    }
}
