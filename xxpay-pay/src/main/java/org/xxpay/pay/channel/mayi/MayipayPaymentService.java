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
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "??????????????????[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "??????????????????????????????";
        JSONObject payInfo = new JSONObject();
        try {

            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//?????????
            map.put("pay_orderid", payOrder.getPayOrderId());
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_amount", String.valueOf(f1));// ?????? ???
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
                    //SDK??????
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payOrder.getChannelId().contains("QR")) {
                    //?????????
                    payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                } else {
                    //????????????
                    if (payJumpUrl.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }

                }
                payInfo.put("payParams", payParams);

                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), channelsn);
                _log.info("[{}]??????????????????????????????:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "????????????[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "????????????!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
        return payInfo;
    }

    /**
     * ????????????
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "??????????????????????????????";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("memberid", channelPayConfig.getMchId());//????????????
            map.put("orderid", payOrder.getPayOrderId());//???????????????
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
            _log.info("?????????????????????" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("retCode");
            if (retCode.equals("10000")) {
                String starus = resObj.getString("status");
                Map sigMap = new HashMap();
                sigMap.put("retCode", resObj.getString("retCode"));//???????????????
                sigMap.put("merchantId", resObj.getString("merchantId"));//????????????
                sigMap.put("outOrderId", resObj.getString("outOrderId"));//???????????????
                sigMap.put("amount", resObj.getString("amount"));//????????????
                sigMap.put("channelsn", resObj.getString("channelsn"));//???????????????
                sigMap.put("status", resObj.getString("status"));//????????????
                sigMap.put("transTime", resObj.getString("transTime"));//????????????????????????
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
                    // ????????????
                    BigDecimal amount = resObj.getBigDecimal("amount");
                    // ????????????
                    long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//??????100
                    long dbPayAmt = payOrder.getAmount().longValue();
                    if (dbPayAmt == payOrder.getAmount()) {
                        if (starus.equals("1")) {
                            //????????????
                            retObj.put("status", "2");
                            retObj.put("msg", "????????????");
                        } else if (starus.equals("3")) {
                            //????????????
                            retObj.put("status", "1");
                            retObj.put("msg", "????????????");
                        } else {
                            //????????????
                            retObj.put("status", "1");
                            retObj.put("msg", "????????????");
                        }
                    } else {
                        retObj.put("status", "1");
                        retObj.put("msg", "????????????????????????????????????????????????");
                    }
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "???????????????");
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "?????????");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "?????????????????????????????????");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

}
