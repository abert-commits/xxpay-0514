package org.xxpay.pay.channel.hongfa;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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
public class HongfaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HongfaPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        retObj = doHuaFeiPayReq(payOrder, "2167");
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【鸿发三方支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getChannelId())) {
                channel = channelPayConfig.getChannelId();
            }

            map.put("customer_no", channelPayConfig.getMchId());// 商家号
            map.put("customer_order", payOrder.getPayOrderId()); //商户订单号
            map.put("produce_date", DateUtil.date2Str(new Date()));
            map.put("bank_code", channel); //支付产品ID

            map.put("amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            map.put("callback_url", payConfig.getReturnUrl(channelName)); //支付结果后台回调URL


            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign_md5", sign); //签名
            map.put("goods_name", "货款");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Defray.html", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            if (res.contains("<html")) {
                String payJumpUrl = res;
                JSONObject payParams = new JSONObject();
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("<form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payParams.put("payJumpUrl", payJumpUrl);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

            } else {

                _log.info("鸿发三方返回失败信息：" + res);
                payInfo.put("errDes", "下单失败[" + res + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:[" + res + "]");
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
        String logPrefix = "【鸿发三方支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("customer_no", channelPayConfig.getMchId());// 商家号
            map.put("customer_order", payOrder.getPayOrderId()); //商户订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign_md5", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Trade_query.html", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String trading_code = resObj.getString("trading_code");
            String retMsg = resObj.getString("trading_state");
            if (trading_code.equals("00")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retMsg + ",订单状态:" + GetStatusMsg(resObj.getString("trading_state")) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "NOTPAY":
                return "未支付";
            case "SUCCESS":
                return "支付成功";
            default:
                return "交易失败";

        }
    }
}
