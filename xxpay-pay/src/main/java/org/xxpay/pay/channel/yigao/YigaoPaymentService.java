package org.xxpay.pay.channel.yigao;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.XxPayPayAppliaction;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

@Service
public class YigaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YigaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YILIANBAO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJuheRyPayReq(payOrder, "ALIPAY_H5");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【艺高统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("merchantNo", channelPayConfig.getMchId().toString());//商户号
            map.put("nonceStr", String.valueOf(System.currentTimeMillis())); //请求13位时间戳
            map.put("paymentType", pay_code); //银行编码
            map.put("mchOrderNo", payOrder.getPayOrderId());
            map.put("orderTime", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYYMMDDHHMMSS));
            map.put("goodsName", "购物");
            map.put("amount", payOrder.getAmount().toString());// 金额 单位分
            map.put("clientIp", "47.56.150.208");
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址

            map.put("buyerId", "B" + String.valueOf(System.currentTimeMillis()));
            map.put("buyerName", "Name" + String.valueOf(System.currentTimeMillis()));


            String sginString = XXPayUtil.mapToString(map).replace(">", "");
            String sign = PayDigestUtil.md5(sginString + "&appkey=" + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api_gateway/pay/order/create", jsonObject.toJSONString(), headers);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String returnCode = resObj.getString("returnCode");
            String retMsg = resObj.getString("returnMsg");
            if (returnCode.equals("FAIL")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String resSign = resObj.getString("sign");
            resObj.remove("sign");
            SortedMap<String, String> resMap = XXPayUtil.JSONObjectToSortedMap(resObj);
            String checkSignStr = XXPayUtil.mapToString(resMap) + "&appkey=" + channelPayConfig.getmD5Key();
            String checkSign = PayDigestUtil.md5(checkSignStr, "UTF-8").toUpperCase();
            if (!checkSign.equals(resSign)) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[上游创单同步返回验签失败]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject payParams = new JSONObject();
            String payUrl = resObj.getString("payUrl");
            String urlType = resObj.getString("urlType");
            payParams.put("payJumpUrl", payUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else {
                if (urlType.equals("URL") || urlType.equals("QR_CODE")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                } else if (urlType.equals("HTMLhtml")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                }
            }

            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("platformOrderNo"));
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
        String logPrefix = "【艺高订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchantNo", channelPayConfig.getMchId());//商户号
            map.put("nonceStr", DateUtil.getRevTime());//随机串
            map.put("mchOrderNo", payOrder.getPayOrderId());//订单号

            String signString = XXPayUtil.mapToString(map).replace(">", "") + "&appkey=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signString, "UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api_gateway/query/payment_order", jsonObj.toJSONString(), headers);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String returnCode = resObj.getString("returnCode");
            String returnMsg = resObj.getString("returnMsg");
            if (returnCode.equals("FAIL")) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败，响应信息:" + returnMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String resSign = resObj.getString("sign");
            resObj.remove("sign");
            SortedMap<String, String> resMap = XXPayUtil.JSONObjectToSortedMap(resObj);
            String checkSignStr = XXPayUtil.mapToString(resMap) + "&appkey=" + channelPayConfig.getmD5Key();
            String checkSign = PayDigestUtil.md5(checkSignStr, "UTF-8").toUpperCase();
            if (!checkSign.equals(resSign)) {
                retObj.put("status", "1");
                retObj.put("msg", "上游查询同步返回,验签失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            if (resObj.getString("orderStatus").equals("SUCCESS")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }

            String status = resObj.getString("orderStatus");
            retObj.put("msg", "响应Code:" + status + ",订单状态:" + GetStatusMsg(status) + "");
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
            case "PROCESSING":
                return "支付中";
            case "SUCCESS":
                return "已支付";
            case "FAIL":
                return "支付失败";
            default:
                return "未支付";
        }
    }


}
