package org.xxpay.pay.channel.zhongyin;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.*;

@Service
public class ZhongyinPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ZhongyinPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ZHONGYING;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "ZFBH5");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【中银付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("amNumber", channelPayConfig.getMchId());//商户编号
            map.put("order_no", payOrder.getPayOrderId());//商户订单号
            map.put("order_amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));// 单位 元
            map.put("product_name", "汉国");
            map.put("pay_type", channel);//支付渠道
            map.put("notify_url", payConfig.getNotifyUrl(channelName));
            map.put("order_time", DateUtil.getSeqString());//订单日期
            map.put("bank_code", "ABC");
            map.put("goods_name", "电脑");
            map.put("remark", "备注");
            String sign = getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("signature", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");

            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/OnlinePay/quickPay", sendMsg);
            _log.info("上游返回信息：" + res);
            if (!res.contains("payUrl")) {
                JSONObject payParams = new JSONObject();
                String payJumpUrl =res;
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
            } else {
                JSONObject resObj = JSONObject.parseObject(res);
                String code = resObj.getString("code");
                String successMessage = resObj.getString("message");
                payInfo.put("errDes", "下单失败[" + successMessage + "]");
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
        String logPrefix = "【中银支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("amNumber", channelPayConfig.getMchId());//商户编号
            map.put("order_no", payOrder.getPayOrderId());//訂單號
            String sign = getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("signature", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/OnlinePay/queryPay", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            if (retCode.equals("00")) {
                //支付成功
                retObj.put("status", "2");
                retObj.put("msg", "支付成功");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }else{
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("mess"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() + "=" + getSortJson((JSONObject) entry.getValue()) + "&");
                } else {
                    list.add(entry.getKey() + "=" + entry.getValue() + "&");
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result = result.substring(0, result.length() - 1);
        result += key;
        _log.info("Sign Before MD5:" + result);
        result = PayDigestUtil.md5(result, "UTF-8").toUpperCase();
        _log.info("Sign Result:" + result);
        return result;
    }

    public static String getSortJson(JSONObject obj) {
        SortedMap map = new TreeMap();
        Set<String> keySet = obj.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            Object vlaue = obj.get(key);
            map.put(key, vlaue);
        }
        return JSONObject.toJSONString(map);
    }
}
