package org.xxpay.pay.channel.juyixin;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.Map;
import java.util.TreeMap;

@Service
public class JuyixinpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JuyixinpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUYIXINPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY: //支付宝 H5
                retObj = doJuYiXinPayReq(payOrder, "1");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doJuYiXinPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【聚义鑫支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new TreeMap();
            map.put("uid", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("price", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //总金额，以元为单位，不允许包括小数点之外的字符。如一分钱为0.01
            map.put("paytype", channel); //支付渠道
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //接收支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); //接收支付界面通知返回地址， url必须为直接可访问的url。
            map.put("user_order_no", payOrder.getPayOrderId()); //商户系统内部的订单号,32个字符内、可包含字母

            StringBuilder sb = new StringBuilder();
            sb.append(channelPayConfig.getMchId());
            sb.append(AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));
            sb.append(channel);
            sb.append(payConfig.getNotifyUrl(getChannelName()));
            sb.append(payConfig.getReturnUrl(getChannelName()));
            sb.append(payOrder.getPayOrderId());
            sb.append(channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(sb.toString(), "utf-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            StringBuffer payForm = new StringBuffer();
            payForm.append("<form>");
            payForm.append("</form>");
            payForm.append("<script> window.onload=function(){ window.location.href='" + channelPayConfig.getReqUrl() + "/pay?" + sendMsg + "' } </script>");

            _log.info(logPrefix + "******************payForm:{}", payForm);
            //表单跳转
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payForm.toString());
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        } catch (
                Exception e) {
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
        String logPrefix = "【聚义鑫支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            Map map = new TreeMap();
            map.put("uid", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("user_order_no", payOrder.getPayOrderId()); //商户系统内部的订单号

            StringBuilder sb = new StringBuilder();
            sb.append(channelPayConfig.getMchId());
            sb.append(payOrder.getPayOrderId());
            sb.append(channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(sb.toString(), "utf-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/stateapi", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String state = resObj.getString("state");
            String Msg = resObj.getString("msg");
            if ("success".equals(state)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Msg:" + Msg);
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Msg:" + Msg);
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
