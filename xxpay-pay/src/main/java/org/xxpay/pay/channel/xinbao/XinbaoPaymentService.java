package org.xxpay.pay.channel.xinbao;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.MchAgentpayPassage;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XinbaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XinbaoPaymentService.class);

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
            //支付宝wap
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "ALI_H5");
                break;
            //支付宝扫码
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doMayiPayReq(payOrder, "901");
                break;
            //微信扫码
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doMayiPayReq(payOrder, "903");
                break;
            //微信wap
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "904");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【鑫宝支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("Version", "1.0");
            map.put("MerNo", channelPayConfig.getMchId());//商户编号
            map.put("PayType", channel);//支付渠道
            map.put("TxSN", payOrder.getPayOrderId());//商户订单号
            map.put("Amount", String.valueOf(payOrder.getAmount()));// 单位 分
            map.put("PdtName", "HX商品购买");//商品名称
            map.put("UserId", DateUtil.getRevTime());
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("NotifyUrl", payConfig.getNotifyUrl(channelName));
            map.put("ReqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            SortedMap<String, String> myMap = XXPayUtil.JSONObjectToSortedMap(map);
            String signStr = XXPayUtil.mapToString(myMap).replace(">", "");
            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("Signature", sign.toLowerCase());
            map.put("SignMethod", "MD5");
            String sendMsg = map.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String data = MyBase64.encodeBase64HttpReq(sendMsg);
            String reqData = "tfTsData=" + data;
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/merpay/api?" + reqData, "");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            if (!resObj.getString("RspCod").equals("00000")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("RspMsg") + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            if (resObj.containsKey("Signature")) {
                //签名校验
                SortedMap<String, String> sortMap = XXPayUtil.JSONObjectToSortedMap(resObj);
                String resSign = resObj.getString("Signature");
                sortMap.remove("Signature");
                sortMap.remove("SignMethod");
                String signStr1 = XXPayUtil.mapToString(sortMap).replace(">", "");
                String signValue = PayDigestUtil.md5(signStr1 + channelPayConfig.getmD5Key(), "utf-8");
                if (!signValue.toUpperCase().equals(resSign.toUpperCase())) {
                    payInfo.put("errDes", "下单失败,\n" + "失败信息：[上游验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            }

            String retCode = resObj.getString("Status");
            if (retCode.equals("1")) {
                String payJumpUrl = resObj.getString("PayUrl");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("RspMsg") + "]");
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
        String logPrefix = "【鑫宝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("MerNo", channelPayConfig.getMchId());//商户编号
            map.put("TxCode", "qrypayord");//交易编码
            map.put("Version", "1.0");
            map.put("TxSN", payOrder.getPayOrderId());

            String signStr = XXPayUtil.mapToString(map).replace(">", "");
            String sign = PayDigestUtil.md5(signStr+channelPayConfig.getmD5Key(),"utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("Signature", sign.toLowerCase());
            map.put("SignMethod", "MD5");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            sendMsg = JSONObject.toJSONString(map);
            String data = MyBase64.encodeBase64HttpReq(sendMsg);
            String reqData = "tfTsData=" + data;
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/merpay/api?" + reqData, "");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("RspCod");
            if (resObj.containsKey("Signature")) {
                //签名校验
                SortedMap<String, String> sortMap = XXPayUtil.JSONObjectToSortedMap(resObj);
                String resSign = resObj.getString("Signature");
                sortMap.remove("Signature");
                sortMap.remove("SignMethod");
                String signStr1 = XXPayUtil.mapToString(sortMap).replace(">", "");
                String signValue = PayDigestUtil.md5(signStr1 + channelPayConfig.getmD5Key(), "utf-8");
                if (!signValue.toUpperCase().equals(resSign.toUpperCase())) {
                    retObj.put("status", "1");
                    retObj.put("msg", "下单失败,\n" + "失败信息：[上游验签失败]");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                    return payInfo;
                }
            }


            if (retCode.equals("00000")) {
                // 订单成功
                if (resObj.getString("Status").equals("1")) {
                    BigDecimal amount = resObj.getBigDecimal("Amount");
                    // 核对金额
                    long outPayAmt = amount.longValue();
                    long dbPayAmt = payOrder.getAmount().longValue();
                    if (outPayAmt == dbPayAmt) {
                        //支付成功
                        retObj.put("status", "2");
                        retObj.put("msg", "支付成功");
                    } else {
                        retObj.put("status", "1");
                        retObj.put("msg", "上游订单金额与本地订单金额不符合");
                    }
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "支付中，上游返回信息：" + resObj.getString("RspMsg"));
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败，失败信息:" + resObj.getString("RspMsg"));
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
