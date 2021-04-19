package org.xxpay.pay.channel.mayi;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

import static org.xxpay.core.common.util.PayDigestUtil.getSortJson;
import static org.xxpay.core.common.util.PayDigestUtil.md5;

@Service
public class MayipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(MayipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "ALIPAY");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDD:
                retObj = doMayiPayReq(payOrder, "ALIPAY_PDD");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XJ:
                retObj = doMayiPayReq(payOrder, "ALIPAY_XJ");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMayiPayReq(payOrder, "ALIPAY_SDK");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doMayiPayReq(payOrder, "ALIPAYQR");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_PDD:
                retObj = doMayiPayReq(payOrder, "WXPAY_PDD");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "WXPAY");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doMayiPayReq(payOrder, "WXPAYZXQR");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_YUNPAY:
                retObj = doMayiPayReq(payOrder, "YUNPAY");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_YUNPAYQR:
                retObj = doMayiPayReq(payOrder, "YUNPAYQR");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay:
                retObj = doMayiPayReq(payOrder, "UnionPay");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【蚂蚁支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {

            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//间连号
            map.put("pay_orderid", payOrder.getPayOrderId());
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_amount", String.valueOf(f1));// 单位 元
            map.put("pay_applydate", DateUtil.getSeqString());
            map.put("pay_bankcode", channel);
            map.put("pay_notifyurl", payConfig.getNotifyUrl(getChannelName()));

            //map.put("pay_callbackurl", payConfig.getReturnUrl(getChannelName()));

            String sign = "";
            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            } else {
                sign = PayDigestUtil.getSignPhP(map, channelPayConfig.getmD5Key());
            }

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String payUrl = "Pay/YZfb/Pay";
            if (!StringUtil.isBlank(channelPayConfig.getRsaprivateKey())) {
                payUrl = channelPayConfig.getRsaprivateKey();
            }
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + payUrl, sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            Integer retCode = resObj.getInteger("retCode");
            String retMsg = resObj.getString("retMsg");
            if (retCode == 10000) {
                JSONObject payParams = new JSONObject();
                //order_str
                String payJumpUrl = "";
                if (resObj.containsKey("order_str")) {
                    payJumpUrl = resObj.getString("order_str");
                } else {
                    payJumpUrl = resObj.getString("payurl");
                }

                String channelsn = resObj.getString("channelsn");
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

                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), channelsn);
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
        String logPrefix = "【蚂蚁支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("memberid", channelPayConfig.getMchId());//商户编号
            map.put("orderid", payOrder.getPayOrderId());//商户订单号
            String sign = "";
            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            } else {
                sign = PayDigestUtil.getSignPhP(map, channelPayConfig.getmD5Key());
            }

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String queryUrl = "/Pay/YZfb/orderQuery";
            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                queryUrl = channelPayConfig.getRsaPublicKey();
            }

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + queryUrl, sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("retCode");
            if (retCode.equals("10000")) {
                String starus = resObj.getString("status");
                Map sigMap = new HashMap();
                sigMap.put("retCode", resObj.getString("retCode"));//处理结果码
                sigMap.put("merchantId", resObj.getString("merchantId"));//商户编号
                sigMap.put("outOrderId", resObj.getString("outOrderId"));//商户订单号
                sigMap.put("amount", resObj.getString("amount"));//订单金额
                sigMap.put("channelsn", resObj.getString("channelsn"));//平台订单号
                sigMap.put("status", resObj.getString("status"));//订单状态
                sigMap.put("transTime", resObj.getString("transTime"));//系统订单完成时间
                sigMap.put("reserved3", resObj.getString("reserved3"));
                sigMap.put("reserved2", resObj.getString("reserved2"));
                sigMap.put("reserved1", resObj.getString("reserved1"));
                sigMap.put("retMsg", resObj.getString("retMsg"));
                String sign1 = "";
                if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                    sign1 = PayDigestUtil.getSign(sigMap, channelPayConfig.getmD5Key());
                } else {
                    sign1 = PayDigestUtil.getSignPhP(sigMap, channelPayConfig.getmD5Key());
                }

                String resSign = resObj.getString("sign");
                if (resSign.equals(sign1)) {
                    // 订单成功
                    BigDecimal amount = resObj.getBigDecimal("amount");
                    // 核对金额
                    long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                    long dbPayAmt = payOrder.getAmount().longValue();
                    if (dbPayAmt == payOrder.getAmount()) {
                        if (starus.equals("1")) {
                            //支付成功
                            retObj.put("status", "2");
                            retObj.put("msg", "支付成功");
                        } else if (starus.equals("3")) {
                            //支付成功
                            retObj.put("status", "1");
                            retObj.put("msg", "支付失败");
                        } else {
                            //支付成功
                            retObj.put("status", "1");
                            retObj.put("msg", "支付失败");
                        }
                    } else {
                        retObj.put("status", "1");
                        retObj.put("msg", "上游订单金额与本地订单金额不符合");
                    }
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "验签未通过");
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

}
