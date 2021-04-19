package org.xxpay.pay.channel.qpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
public class QpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HANXIN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doQpayPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doQpayPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【Q支付手淘红包统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("user_id", channelPayConfig.getMchId()); // 用户ID
            map.put("out_trade_no", payOrder.getPayOrderId()); // 订单编号
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); // 金额
            map.put("channel_code", channelPayConfig.getChannelId()); // 渠道代码
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); // 通知地址
            map.put("return_url", payConfig.getReturnUrl(channelName)); // 回跳地址
            map.put("secret", channelPayConfig.getmD5Key()); // 秘钥串，签名使用

            String signStr = XXPayUtil.mapToStringByURLcode(map);
            String sign = PayDigestUtil.md5(signStr, "UTF-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.remove("secret");
            map.put("signature", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/order", sendMsg);
//            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/gateway/createOrder", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retError = resObj.getString("error");
//            String retMsg = resObj.getString("msg");
//            JSONObject retData = resObj.getJSONObject("data");

            if (StringUtils.isBlank(retError)) {
                String payJumpUrl = resObj.getString("pay_url");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_id"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + retError + "]");
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
        String logPrefix = "【Q支付手淘红包订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("user_id", channelPayConfig.getMchId()); // 用户ID
            map.put("out_trade_no", payOrder.getPayOrderId()); // 订单编号
            map.put("secret", channelPayConfig.getmD5Key()); // 秘钥串，签名使用

            String signStr = XXPayUtil.mapToStringByURLcode(map);
            String sign = PayDigestUtil.md5(signStr, "UTF-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.remove("secret");
            map.put("signature", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/queryOrder", sendMsg);
//            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/gateway/queryOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retError = resObj.getString("error");

            if (StringUtils.isBlank(retError)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + "2" + ",订单状态:" + GetStatusMsg(resObj.getString("status")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + "1" + ",订单状态:" + retError + "");
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
            case "Pending Create":
                return "待下单（未扫码）";
            case "Exception":
                return "异常";
            case "Pending Payment":
                return "待付款";
            case "Expired":
                return "已过期";
            case "Completed":
                return "已完成";
            default:
                return "异常订单";
        }
    }
}
