package org.xxpay.pay.channel.yunfeipay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class YunfeipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YunfeipayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YUNFEIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doYunfeipayRyPayReq(payOrder, "1"); //pay.alipay.scan支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doYunfeipayRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【云飞支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
//            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
//            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("mch_id", channelPayConfig.getMchId());//商户号
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); //请求时间戳
            map.put("noncestr", RandomStringUtils.randomAlphanumeric(32));
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("custom_sn", payOrder.getPayOrderId());// 商户订单号
            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                pay_code = channelPayConfig.getRsaPublicKey();
            }
            map.put("type", pay_code);//支付方式
            map.put("money", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 金额
              String sign = PayDigestUtil.md5(channelPayConfig.getmD5Key() + payOrder.getPayOrderId() +channelPayConfig.getMchId() +AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())) +map.get("noncestr")
                      + payConfig.getNotifyUrl(channelName) +String.valueOf(System.currentTimeMillis() / 1000) +pay_code + channelPayConfig.getmD5Key() ,"UTF-8" ).toLowerCase();
//            String sign = getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
//            String sign = PayDigestUtil.getSign(map,channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/app/get_qrcode", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            if (resObj.getString("code").equals("200")) {
                String payJumpUrl = resObj.getJSONObject("data").getString("pay_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                //表单跳转
                if (payJumpUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + res + "]");
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

    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(PayDigestUtil.getSortJson((JSONObject) entry.getValue()));
                } else {
                    list.add(entry.getValue() +"");
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
        String result = sb.toString().substring(0,sb.length()-1);
        result = key + result + key;
        _log.info("Sign Before MD5:" + result);
        result = PayDigestUtil.md5(result, "UTF-8").toUpperCase();
        _log.info("Sign Result:" + result);
        return result;
    }

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【云飞支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());//商户号
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); //请求时间戳
            map.put("noncestr", RandomStringUtils.randomAlphanumeric(32));
            map.put("custom_sn", payOrder.getPayOrderId());// 商户订单号
            map.put("trade_sn", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 金额
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/app/get_order_detail", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");
            if (code.equals("200")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("respCode", resObj.getString("respCode"));//商户编号
                map1.put("msg", resObj.getString("msg"));//平台订单号
                map1.put("out_order_no", resObj.getString("out_order_no"));//订单状态
                map1.put("sys_order_no", resObj.getString("sys_order_no"));//订单实际金额
                map1.put("status", resObj.getString("status"));//状态码
                map1.put("money", resObj.getString("money"));//说明
                map1.put("realPrice", resObj.getString("realPrice"));//说明
                System.out.println("云飞------》》》》》》《《《《《《++++++" + map1.toString());
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key()).toLowerCase();
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            }
            if (resObj.getString("status").equals("1")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }

            String status = resObj.getString("status");
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

    private String GetStatusMsg(String code) {
        switch (code) {
            case "1":
                return "支付成功";
            case "2":
                return "支付异常";
            case "3":
                return "未支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
