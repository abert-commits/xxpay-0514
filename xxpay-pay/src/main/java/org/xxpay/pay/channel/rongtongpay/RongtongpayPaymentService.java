package org.xxpay.pay.channel.rongtongpay;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.xxpay.core.common.util.PayDigestUtil.getSortJson;
import static org.xxpay.core.common.util.PayDigestUtil.md5;

@Service
public class RongtongpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(RongtongpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DOUDOU;
    }

    private static String encodingCharset = "UTF-8";
    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doDouDouRyPayReq(payOrder, ""); //融通话费H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doDouDouRyPayReq(payOrder, ""); //融通话费H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDouDouRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【融通支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
           map.put("merchant_id", channelPayConfig.getMchId());//商户号
            map.put("version","V2.0");
            map.put("pay_type", channelPayConfig.getChannelId()); //银行编码
            map.put("device_type","wap"); //终端类型
            map.put("request_time", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYYMMDDHHMMSS)); //请求时间
            map.put("nonce_str", DateUtil.getRevTime());
            map.put("pay_ip",payOrder.getClientIp());//客户端Ip
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            map.put("amount", String.valueOf(amount));// 金额
            map.put("currency", "CNY");

            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
           /* map.put("money",amount);
            map.put("part_sn",payOrder.getPayOrderId());
            map.put("notify",payConfig.getNotifyUrl(channelName));
            map.put("id",channelPayConfig.getMchId());
*/

            String sign = PayDigestUtil.getSignNotKey(map,channelPayConfig.getmD5Key()).toUpperCase();;
            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = JSONObject.toJSONString(map);


            String res = (String) XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/gateway/dopay",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("status").equals("success")) {
                String pay_url= resObj.getString("sdk_str");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", pay_url);
                if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else {
                    if (pay_url.contains("<form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
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
    public static String getSign(Map<String, Object> map, String key) throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() +  getSortJson((JSONObject) entry.getValue()) );
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
        result =key+result+ key;
        _log.info("Sign Before MD5:" + result);
        result = md5(result, encodingCharset).toLowerCase();
        _log.info("Sign Result:" + result);
        return result;
    }
    /**
     * 查询订单
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【融通支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
           ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchant_id", channelPayConfig.getMchId());//商户号
            map.put("version", "V2.0");//商户号
            map.put("out_trade_no", payOrder.getPayOrderId());//商户号
            map.put("request_time",  DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYYMMDDHHMMSS));//商户号
            map.put("nonce_str", DateUtil.getRevTime());//商户号
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);


            String sendMsg =JSONObject.toJSONString(map);
            String res = (String) XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/gateway/search_order",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String status = resObj.getString("status");
            if (status.equals("success")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }

            String pay_status=resObj.getString("pay_status");
            retObj.put("msg", "订单状态:"+GetStatusMsg(pay_status)+"");
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
            case "success":
                return "已支付";
            case "fail":
                return "支付失败";
            case "waiting":
                return "支付中";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
