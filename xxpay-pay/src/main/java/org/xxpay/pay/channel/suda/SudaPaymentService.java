package org.xxpay.pay.channel.suda;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.codec.Base64;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Service
public class SudaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(SudaPaymentService.class);

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
            //支付宝SDK
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【速达支付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap();


            String orderId = URLEncoder.encode(Base64Utils.encode(RSAPKCS1PaddingwithRSAUtils.encryptByPublicKeyBy256(payOrder.getPayOrderId().getBytes("UTF-8"), channelPayConfig.getRsaPublicKey())), "UTF-8");
            map.put("id", orderId);//商户订单号
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            amount = URLEncoder.encode(Base64Utils.encode(RSAPKCS1PaddingwithRSAUtils.encryptByPublicKeyBy256(amount.getBytes("UTF-8"), channelPayConfig.getRsaPublicKey())), "UTF-8");
            map.put("payAmount", amount);// 单位 元
            map.put("userId", channelPayConfig.getMchId());//商户编号
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            String notifyUrl = payConfig.getNotifyUrl(channelName);

            notifyUrl = URLEncoder.encode(Base64Utils.encode(RSAPKCS1PaddingwithRSAUtils.encryptByPublicKeyBy256(notifyUrl.getBytes("UTF-8"), channelPayConfig.getRsaPublicKey())), "UTF-8");
            map.put("notifyUrl", notifyUrl);

            String returnUrl = "127.0.0.1";
            returnUrl = URLEncoder.encode(Base64Utils.encode(RSAPKCS1PaddingwithRSAUtils.encryptByPublicKeyBy256(returnUrl.getBytes("UTF-8"), channelPayConfig.getRsaPublicKey())), "UTF-8");
            map.put("returnUrl", returnUrl);
            //通道类型
            String payType = "Bank";
            payType = URLEncoder.encode(Base64Utils.encode(RSAPKCS1PaddingwithRSAUtils.encryptByPublicKeyBy256(payType.getBytes("UTF-8"), channelPayConfig.getRsaPublicKey())), "UTF-8");
            map.put("payType", payType);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = HttpClient.callHttpPost(channelPayConfig.getReqUrl() + "/api/Pay/createJson.do?" + sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject retObj = JSONObject.parseObject(res);
            String code = retObj.getString("result");
            String retMsg = retObj.getString("message");
            if (code.equals("error")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject datamap = retObj.getJSONObject("datamap");

            JSONObject payParams = new JSONObject();
            String payJumpUrl = datamap.getString("url");
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (res.contains("form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }

            payParams.put("payJumpUrl", payJumpUrl);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
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
        String logPrefix = "【速达支付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("userId", channelPayConfig.getMchId());//商户编号
            map.put("id", payOrder.getPayOrderId());//上游訂單號
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/Pay/query.do", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("result");
            if (retCode.equals("error")) {
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("message"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
            if (retCode.equals("success")) {

                JSONObject datamap = resObj.getJSONObject("datamap");
                String payStatus = datamap.getString("payStatus");
                payStatus = RSAPKCS1PaddingwithRSAUtils.decryptByPublicKey(payStatus, channelPayConfig.getRsaPublicKey(),"UTF-8");

                if (payStatus.equals("1")) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", GetStatusMsg(payStatus));
                }

            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }


    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "待付款";
            case "1":
                return "付款成功";
            case "2":
                return "付款失败";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }

}
