package org.xxpay.pay.channel.jpay;

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

@Service
public class JpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doPayReq(payOrder, "pddsdk");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doPayReq(payOrder, "pddali");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【JPAY支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("method", "bill.trade.nobank");//接口服务码
            map.put("mchId", channelPayConfig.getMchId());//商户编号
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));// 单位 元
            map.put("appType", channel);//支付类型
            map.put("orderId", payOrder.getPayOrderId());//商户订单号
            map.put("orderUid", "127.0.0.1");//商户会员IP
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));
            map.put("returnUrl", payConfig.getReturnUrl(channelName));

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String msg = resObj.getString("msg");

            if (retCode.equals("SUCCESS")) {
                String signs = resObj.getString("sign").toUpperCase();
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {
                    String payJumpUrl = resObj.getString("payUrl");
                    String orderid = resObj.getString("tradeNo");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payOrder.getChannelId().contains("SDK")) {
                        //SDK跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payOrder.getChannelId().contains("QR")) {
                        //二维码
                        payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                    } else {
                        //表单跳转
                        if (payJumpUrl.contains("form")) {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                        } else {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                        }
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), orderid);
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                payInfo.put("errDes", "下单失败,\n" + "失败原因[" + msg + "]");
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
        String logPrefix = "【JPAY訂單查詢】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("method", "bill.query");//接口服务码
            map.put("mchId", channelPayConfig.getMchId());//商户编号
            map.put("orderId", payOrder.getPayOrderId());//上游訂單號

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String msg = resObj.getString("msg");

            if (retCode.equals("SUCCESS")) {
                String signs = resObj.getString("sign").toUpperCase();
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {
                    // 订单成功
                    BigDecimal amount = resObj.getBigDecimal("amount");
                    // 核对金额
                    long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                    long dbPayAmt = payOrder.getAmount().longValue();
                    if (dbPayAmt == payOrder.getAmount()) {
                        //支付成功
                        retObj.put("status", "2");
                        retObj.put("msg", GetStatusMsg(resObj.getString("status")));
                    } else {
                        retObj.put("status", "1");
                        retObj.put("msg", "上游订单金额与本地金额不符，上游订单金额：" + dbPayAmt / 100 + "");
                    }
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }

            } else {
                retObj.put("msg", msg);
                retObj.put("status", "1");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游发生异常!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String status) {
        switch (status) {
            case "SUCCESS":
                return "支付成功";
            case "CLOSED":
                return "支付失败/超时关闭";
            case "NOTPAY":
                return "待支付";
            default:
                return "部分中间状态本文档可能暂未列出";
        }
    }
}
