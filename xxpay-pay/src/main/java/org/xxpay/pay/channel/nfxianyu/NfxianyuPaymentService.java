package org.xxpay.pay.channel.nfxianyu;

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
public class NfxianyuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(NfxianyuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_KUKU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuaFeiPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【nf咸鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());
            map.put("amount", payOrder.getAmount()); //支付金额
            map.put("orderNo", payOrder.getPayOrderId());
            map.put("createTime",  DateUtil.date2Str(new Date())); //应用ID
            map.put("returnUrl", payConfig.getReturnUrl(channelName)); //支付结果后台回调URL
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));

            String sendMsg = XXPayUtil.mapToStringO(map).replace(">", "");

            String sign = PayDigestUtil.String2SHA256(sendMsg+"&key="+channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            sendMsg = XXPayUtil.mapToStringO(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/yeapay/pay.do", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("result_code");
            String retMsg = resObj.getString("result_msg");

            if ("0000".equals(resultCode)) {
                String payJumpUrl = resObj.getString("qr_code");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
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
        String logPrefix = "【nf咸鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId()); //应用ID
            map.put("orderNo", payOrder.getPayOrderId());
            map.put("createTime", DateUtil.date2Str(new Date()));

            String sendMsg = XXPayUtil.mapToStringO(map).replace(">", "");
            String sign = PayDigestUtil.String2SHA256(sendMsg+"&key="+channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            sendMsg = XXPayUtil.mapToStringO(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/yeapay/query.do", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("result_code");
            String retMsg = resObj.getString("result_msg");
            if (retCode.equals("0000")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getString("state") )+ "");
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
            case "TRADE_SUCCESS":
                return "支付成功";
            case "PROCESSING":
                return "订单处理中";
            case "REFUND":
                return "退款订单";
            case "UNPAID":
                return "未支付订单";
            default:
                return "异常订单";
        }
    }
}
