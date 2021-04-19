package org.xxpay.pay.channel.dingshengpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class DsxjPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DsxjPaymentService.class);

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
        String logPrefix = "【鼎盛现金红包统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("mch_no", channelPayConfig.getMchId()); // 商户编号
            map.put("app_id", channelPayConfig.getRsaPublicKey()); // 应用ID
            map.put("nonce_str", RandomStringUtils.randomAlphanumeric(16)); // 随机字符串
            map.put("trade_type", channelPayConfig.getChannelId()); // 交易方式
            map.put("total_fee", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("body", "鼎盛现金红包"); // 订单描述
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); // 通知地址
            map.put("front_url", payConfig.getReturnUrl(channelName)); // 通知地址
            map.put("out_trade_no", payOrder.getPayOrderId()); // 业务订单号
            map.put("spbill_create_ip", payOrder.getClientIp()); // 用户IP

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay-api/unified/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("return_code");
            String retMsg = resObj.getString("return_msg");

            if ("0000".equals(resultCode)) {
                String payJumpUrl = resObj.getString("pay_info");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("<form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("thd_trans_no"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
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
        String logPrefix = "【鼎盛现金红包订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("mch_no", channelPayConfig.getMchId()); // 商户编号
            map.put("app_id", channelPayConfig.getRsaPublicKey()); // 应用ID
            map.put("nonce_str", RandomStringUtils.randomAlphanumeric(16)); // 随机字符串
            map.put("out_trade_no", payOrder.getPayOrderId()); // 业务订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay-api/unified/query", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("return_code");
            String retMsg = resObj.getString("return_msg");

            if ("0000".equals(resultCode)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + resultCode + ",订单状态:" + GetStatusMsg(resObj.getString("trade_status")) + "");
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
            case "SUCCESS":
                return "成功";
            case "USERPAYING":
                return "支付中";
            case "FALUER":
                return "支付失败";
            case "SUBMIT":
                return "已提交";
            case "OVERDUE":
                return "订单过期";
            default:
                return "异常订单";
        }
    }
}
