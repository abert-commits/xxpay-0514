package org.xxpay.pay.channel.JooE;


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

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

@Service//shihuangjingdongPayNotifyService
public class JooeePaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JooeePaymentService.class);

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
            //微信wap
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
                retObj = doJooEPayReq(payOrder, "");//917
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doJooEPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【JooE卡统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId()); // 商户号
            map.put("methond", "submitorder"); // 	请求方法
            map.put("orderid", payOrder.getPayOrderId()); // 订单号
            map.put("channel_code", channelPayConfig.getChannelId()); // 订单内容
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("notifyurl", payConfig.getNotifyUrl(channelName)); // 支付结果后台回调URL

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/", sendMsg, "UTF-8", "application/x-www-form-urlencoded");
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");
            if ("1".equals(retCode)) {
                String payJumpUrl = retData.getString("url");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), retData.getString("orderid"));
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
        String logPrefix = "【JooE卡订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();

        retObj.put("status", "2");
        retObj.put("msg", "上游没有查询接口");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;

//        try {
//            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
//
//            SortedMap map = new TreeMap();
//            map.put("appid", channelPayConfig.getMchId()); // 商户号
//            map.put("methond", "orderstatus"); // 	请求方法
//            map.put("orderid", payOrder.getPayOrderId()); // 订单号
//
//            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
//            _log.info(logPrefix + "******************sign:{}", sign);
//            map.put("sign", sign);
//
//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//
//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/", sendMsg, "UTF-8", "application/x-www-form-urlencoded");
//            _log.info("上游返回信息：" + res);
//            JSONObject resObj = JSONObject.parseObject(res);
//
//            String retCode = resObj.getString("code");
//            String retMsg = resObj.getString("msg");
//            JSONObject retData = resObj.getJSONObject("data");
//            if ("1".equals(retCode)) {
//                retObj.put("status", "2");
//                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retData.getString("status")) + "");
//            } else {
//                retObj.put("status", "1");
//                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
//            }
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            return retObj;
//        } catch (Exception e) {
//            retObj.put("errDes", "操作失败!");
//            retObj.put("msg", "查询系统：请求上游发生异常！");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//            return retObj;
//        }
    }

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "待支付";
            case "1":
                return "已支付";
            case "2":
                return "已完成";
            default:
                return "异常订单";
        }
    }
}
