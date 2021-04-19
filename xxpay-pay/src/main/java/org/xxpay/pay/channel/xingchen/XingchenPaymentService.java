package org.xxpay.pay.channel.xingchen;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLEncoder;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class XingchenPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XingchenPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XINGCHEN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doXingChenPayReq(payOrder, "");//ALISCAN 支付宝扫码支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doXingChenPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【星辰支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String money = AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()));

            SortedMap map = new TreeMap();
            map.put("app_id", channelPayConfig.getMchId()); // 商户号
            map.put("channel_no", channelPayConfig.getChannelId()); // 支付通道编码
            map.put("money", money); // 支付金额
            map.put("title", "支付"+money+"元");
            map.put("out_order_no", payOrder.getPayOrderId()); // 商户平台订单号
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); // 异步回调地址
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); // 同步跳转地址
            map.put("is_poll", "alipay");
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/jhpay/order",sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");
            if ("0".equals(resultCode)) {
                String payJumpUrl = retData.getString("url");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), retData.getString("no"));
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
        String logPrefix = "【星辰支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("app_id", channelPayConfig.getMchId()); // 商户号
            map.put("order_no", payOrder.getChannelOrderNo());
            map.put("out_order_no", payOrder.getPayOrderId()); // 商户平台订单号

            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/jhpay/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");

            if ("0".equals(resultCode)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + retData.getString("pay_cn_status") + "");
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
//    private String GetStatusMsg(String code) {
//        switch (code) {
//            case "-1":
//                return "未支付";
//            case "200":
//                return "支付成功";
//            default:
//                return "异常订单";
//        }
//    }
}
