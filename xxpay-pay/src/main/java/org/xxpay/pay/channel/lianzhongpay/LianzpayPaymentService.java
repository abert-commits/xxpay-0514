package org.xxpay.pay.channel.lianzhongpay;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class LianzpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LianzpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LIANZHONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doLianzPayReq(payOrder, "8006");//支付宝SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doLianzPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【联众支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("mer_id", channelPayConfig.getMchId()); // 商户号
            map.put("business_number", payOrder.getPayOrderId()); //商户订单号
            map.put("channel_no", channelPayConfig.getChannelId()); // 管道号
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("back_url", payConfig.getReturnUrl(getChannelName())); //返回地址
            map.put("server_url", payConfig.getNotifyUrl(getChannelName())); //请求地址
            map.put("time_stamp", String.valueOf(System.currentTimeMillis()/1000)); // 订单时间

            String signString = XXPayUtil.mapToStringEmity(map);
            String sign = PayDigestUtil.md5(signString+"&"+channelPayConfig.getmD5Key(), "utf-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            map.put("sign_type", "md5");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/customerPay", sendMsg, "utf-8", "application/x-www-form-urlencoded");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");

            if ("0".equals(resultCode)) {
                String payUrl = retData.getString("trade_qrcode");
                JSONObject payParams = new JSONObject();
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    payParams.put("payJumpUrl", payUrl);
                } else if (payUrl.contains("<form")) {
                    payParams.put("payJumpUrl", payUrl);
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payJumpUrl", payUrl);
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);

            }
            return payInfo;
        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
    }

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【联众支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mer_id", channelPayConfig.getMchId());// 商家号
            map.put("business_number", payOrder.getPayOrderId()); //商户订单号
            map.put("time_stamp", String.valueOf(System.currentTimeMillis()/1000)); //商户订单号

            String signString = XXPayUtil.mapToStringEmity(map);
            String sign = PayDigestUtil.md5(signString+"&"+channelPayConfig.getmD5Key(), "utf-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
            map.put("sign_type","md5");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/customerPayQuery", sendMsg, "utf-8", "application/x-www-form-urlencoded");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("MSG_OK")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getString("pay_status")) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String  code) {
        switch (code) {
            case "20":
                return "支付成功";
            case "30":
                return "支付失败";
            case "40":
                return "未支付";
            default:
                return "状态不明";
        }
    }
}
