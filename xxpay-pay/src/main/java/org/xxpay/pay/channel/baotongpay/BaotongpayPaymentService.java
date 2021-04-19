package org.xxpay.pay.channel.baotongpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

@Service
public class BaotongpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BaotongpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_BAOTONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doBaotongRyPayReq(payOrder, "10"); //支付宝拼多多SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doBaotongRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【宝通支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();

            map.put("reqCmd", "req.trade.order");// 商家号
            map.put("merchNo", channelPayConfig.getMchId());// 商家号
            map.put("charset", "UTF-8");// 商家号
            map.put("signType", "MD5");// 商家号
            map.put("reqIp", "127.0.0.1");// 商家号
            map.put("payType", pay_code); //支付方式
            map.put("tradeNo", payOrder.getPayOrderId()); //订单编号
            map.put("currency", "CNY");// 商家号
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));  //支付金额
            map.put("userId", "456");// 商家号
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("returnUrl", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            map.put("goodsName", "一条裤子");
            map.put("goodsDesc", "sdk");
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/xpay/xpayapi", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            String status = resObj.getString("status");
            if ("10000".equals(resultCode) && status.equals("1")) {
                String payJumpUrl = resObj.getString("bankUrl");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_sn"));
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
     * @param
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【宝通支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("reqCmd", "req.query.trade");//商户编号
            map.put("merchNo", channelPayConfig.getMchId());//商户编号
            map.put("charset", "utf-8");//商户编号
            map.put("signType", "MD5");//MD5
            map.put("reqIp", "127.0..0..1");//請求公網地址
            map.put("tradeNo", payOrder.getChannelOrderNo());//随机码
            map.put("remark", "韓信四方");//商户编号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/orderinfo", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String status = resObj.getString("status");
            if (code.equals("10000") && status.equals("2")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("code", resObj.getString("code"));//商户编号
                map1.put("msg", resObj.getString("msg"));
                map1.put("merchNo", resObj.getString("merchNo"));
                map1.put("amount", resObj.getString("amount"));
                map1.put("tradeNo", resObj.getString("tradeNo"));
                map1.put("orderNo", resObj.getString("orderNo"));
                map1.put("status", resObj.getString("status"));
                map1.put("remark", resObj.getString("remark"));
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            } else {
                if (resObj.getString("status").equals("2")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
            }
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() + PayDigestUtil.getSortJson((JSONObject) entry.getValue()));
                } else {
                    list.add(entry.getKey() + entry.getValue());
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
        result = key + result + key;
        _log.info("Sign Before MD5:" + result);
        result = PayDigestUtil.md5(result, "UTF-8").toUpperCase();
        _log.info("Sign Result:" + result);
        return result;
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "订单生成";
            case "1":
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "业务处理完成";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
