package org.xxpay.pay.channel.kangxi;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class KangxiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(KangxiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_KANGXI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doLaLaPayReq(payOrder, "");//ALISCAN 支付宝扫码支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doLaLaPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【康熙支付统一下单】";
        JSONObject payInfo = new JSONObject();
        String money = AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()));
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();

            map.put("mch_id", channelPayConfig.getMchId()); // 商户号
            map.put("pass_code", channelPayConfig.getChannelId()); // 通道类型
            map.put("subject", "支付"+money+"元"); // 订单标题
            map.put("body", "支付"+money+"元"); // 订单描述
            map.put("out_trade_no", payOrder.getPayOrderId()); // 商户订单号
            map.put("amount", money); // 支付金额
            map.put("client_ip", payOrder.getClientIp()); // 客户IP地址
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); // 异步回调地址
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); // 异步回调地址
            map.put("timestamp", DateUtil.date2Str(new Date())); // 发送请求的时间

            String reString = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(reString+channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/unifiedorder",sendMsg, "utf-8");
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/unifiedorder", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");
            if ("0".equals(resultCode)) {
                String payJumpUrl = retData.getString("pay_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl",payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("<form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_sn"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败["+retMsg+"]");
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
        String logPrefix = "【康熙支付订单查询】";
        JSONObject retObj = new JSONObject();

        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId()); // 商户号
            map.put("out_trade_no", payOrder.getPayOrderId()); // 商户订单号
            map.put("timestamp", DateUtil.date2Str(new Date()));

            String reString = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(reString+channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");

            if ("0".equals(resultCode)) {
                retObj.put("status", 2);
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + GetStatusMsg(retData.getString("status")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + retMsg + "");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "支付失败";
            case "1":
                return "待支付";
            case "2":
                return "支付成功";
            default:
                return "异常订单";
        }
    }
}
