package org.xxpay.pay.channel.wuxing;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class WuxingPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(WuxingPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WUXING;
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
                retObj = doWuXingPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doWuXingPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【五行微信话费直充统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("app_id", channelPayConfig.getMchId()); // 商户ID
            map.put("amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("out_trade_id", payOrder.getPayOrderId());
            map.put("payment_code", channelPayConfig.getChannelId());
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("callback_url", payConfig.getReturnUrl(getChannelName())); // 付款结束后，客户浏览器的跳转地址

            String mapString = XXPayUtil.mapToString(map);
            _log.info(logPrefix + "******************mapToString:{}", mapString);

            String sign=PayDigestUtil.md5(mapString + "&app_secret=" + channelPayConfig.getmD5Key(),"UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("md5_sign", sign); //签名


            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/merchant/payOrder", sendMsg, "utf-8", "application/x-www-form-urlencoded");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
//            String retMsg = resObj.getString("msg");
//            JSONObject retData = resObj.getJSONObject("data");
            if ("200".equals(resultCode)) {
                String payJumpUrl = resObj.getString("data");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + resObj.getString("err") + "]");
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
        String logPrefix = "【五行微信话费直充订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("app_id", channelPayConfig.getMchId());
            map.put("out_trade_id", payOrder.getPayOrderId());

            String mapString = XXPayUtil.mapToString(map);
            _log.info(logPrefix + "******************mapToString:{}", mapString);
            String sign=PayDigestUtil.md5(mapString + "&app_secret=" + channelPayConfig.getmD5Key(),"UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("md5_sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/merchant/queryOrder", sendMsg, "utf-8", "application/x-www-form-urlencoded");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
//            String retMsg = resObj.getString("msg");
//            JSONObject retData = resObj.getJSONObject("data");

            if ("200".equals(resultCode)) {
                retObj.put("status", 2);
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + GetStatusMsg(resObj.getString("order_status")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + resObj.getString("err") + "");
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
            case "1":
                return "待付款";
            case "2":
                return "支付成功";
            case "3":
                return "支付失败";
            case "4":
                return "支付取消";
            case "5":
                return "支付异常";
            default:
                return "未知状态";
        }
    }
}
