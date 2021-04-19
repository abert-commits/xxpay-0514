package org.xxpay.pay.channel.haitian;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.lang3.RandomStringUtils;
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
public class HaitianPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HaitianPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HAITIAN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHaiTianPayReq(payOrder, "");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHaiTianPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【海天支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("mch_id", channelPayConfig.getMchId()); //商户账户
            map.put("ptype", channelPayConfig.getChannelId()); // 支付类型
            map.put("order_sn", payOrder.getPayOrderId()); // 订单号
            map.put("money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("goods_desc", "buy"); // 商品描述
            map.put("client_ip", "127.0.0.1"); // Ip地址
            map.put("format", "page"); // 接口返回格式
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); // 异步通知地址
            map.put("time", String.valueOf(System.currentTimeMillis()/1000)); // 时间戳

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String formHtml= XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/?c=Pay");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", formHtml);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            String sendMsg = JSON.toJSONString(map);
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//
//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/?c=Pay", sendMsg, "utf-8");
//            _log.info(logPrefix + "******************上游返回数据:{}", res);
//
//            JSONObject resObj = JSONObject.parseObject(res);
//            String resultCode = resObj.getString("resp_code");
//            String retMsg = resObj.getString("resp_message");
//
//            if ("1000".equals(resultCode)) {
//                String payJumpUrl = resObj.getJSONObject("data").getString("pay_info");
//                JSONObject payParams = new JSONObject();
//                payParams.put("payJumpUrl", payJumpUrl);
//                if (payOrder.getChannelId().contains("SDK")) {
//                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
//                } else if (payJumpUrl.contains("form")) {
//                    //表单跳转
//                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
//                } else {
//                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
//                }
//                payInfo.put("payParams", payParams);
//                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
//                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            } else {
//                payInfo.put("errDes", "下单失败[" + retMsg + "]");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }
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
        String logPrefix = "【海天支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("mch_id", channelPayConfig.getMchId()); // 商户号
            map.put("out_order_sn", payOrder.getPayOrderId()); // 商户单号
            map.put("time", String.valueOf(System.currentTimeMillis()/1000));

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            String sendMsg = JSON.toJSONString(map);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "?c=Pay&a=query", sendMsg, "utf-8");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");

            if ("1".equals(retCode) && "ok".equals(retMsg)) {
                if ("9".equals(retData.getString("status"))) {
                    retObj.put("status", "2");
                    retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retData.getString("status_flag") + "");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retData.getString("status_flag") + "");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            }
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
            case "1000":
                return "支付成功";
            case "1002":
                return "支付失败";
            case "1010":
                return "未支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
