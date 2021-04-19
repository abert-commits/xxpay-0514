package org.xxpay.pay.channel.qiangu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
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
public class QgfishPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QgfishPaymentService.class);

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
                retObj = doQianGuPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doQianGuPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【新千古闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("merchant_id", channelPayConfig.getMchId()); // 商户号
            map.put("version", "V2.0");//版本编号
            map.put("pay_type", channelPayConfig.getChannelId()); // 支付方式
            map.put("device_type", "pc"); // 支付设备
            map.put("request_time", DateUtils.getTimeStr(new Date(), "yyyyMMddHHmmss")); // 请求时间
            map.put("nonce_str", RandomStrUtils.getInstance().getRandomString(16)); // 随机字符串
            map.put("pay_ip", payOrder.getClientIp()); // 支付IP
            map.put("out_trade_no", payOrder.getPayOrderId()); // 订单号
            map.put("amount", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())); //支付金额
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); // 支付结果后台回调URL

            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/createOrder", sendMsg, "utf-8");
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/gateway/dopay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retStatus = resObj.getString("status");
            String retMsg = resObj.getString("message");
            if ("success".equals(retStatus)) {
                String payJumpUrl = resObj.getString("sdk_str");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("trade_no"));
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
        String logPrefix = "【新千古闲鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("merchant_id", channelPayConfig.getMchId()); // 商户号
            map.put("version", "V2.0");//版本编号
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号
            map.put("request_time", DateUtils.getTimeStr(new Date(), "yyyyMMddHHmmss")); // 请求时间
            map.put("nonce_str", RandomStrUtils.getInstance().getRandomString(16)); // 随机字符串

            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/queryOrder", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/gateway/search_order", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retStatus = resObj.getString("status");
            String retMsg = resObj.getString("message");
            if ("success".equals(retStatus)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + retStatus + ",订单状态:" + GetStatusMsg(resObj.getString("pay_status")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retStatus + ",订单状态:" + retMsg + "");
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
            case "success":
                return "已支付";
            case "fail":
                return "支付失败";
            case "waiting":
                return "支付中";
            default:
                return "异常订单";
        }
    }
}
