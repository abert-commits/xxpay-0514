package org.xxpay.pay.channel.chuangshiji;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ChuangshijiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ChuangshijiPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DASHEN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doPayReq(payOrder, "8");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【创世纪支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            SortedMap jsonObject = new TreeMap();
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            jsonObject.put("merchno", channelPayConfig.getMchId());//商户号
            jsonObject.put("orderId", payOrder.getPayOrderId());// 商户订单号
            if (!StringUtils.isEmpty(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            jsonObject.put("payType", pay_code);//支付类型
            jsonObject.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//订单金额
            jsonObject.put("asyncUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            jsonObject.put("requestTime", DateUtil.getRevTime());//请求时间
            jsonObject.put("attach", "Shopping");//商品描述


            String signStr = XXPayUtil.mapToString(jsonObject) + "&secretKey=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8").toLowerCase();
            jsonObject.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(jsonObject);
            _log.info(logPrefix + "******************请求数据:SendMsg={}", sendMsg);
            String fromJump = XXPayUtil.buildRequestHtml(jsonObject, channelPayConfig.getReqUrl() + "/api/order/placeOrder");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", fromJump);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
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

    public static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错诿,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }


    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【创世纪支付订单查询】";
        SortedMap jsonObject = new TreeMap();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            jsonObject.put("merchno", channelPayConfig.getMchId());// 订单号
            jsonObject.put("orderId", payOrder.getPayOrderId()); // 商户号
            jsonObject.put("requestTime", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYYMMDDHHMMSS));//请求时间


            String signStr = XXPayUtil.mapToString(jsonObject) + "&secretKey=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8").toLowerCase();
            jsonObject.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(jsonObject);
            _log.info(logPrefix + "******************请求数据:SendMsg={}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/order/queryOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String msg = resObj.getString("msg");
            if (code.equals("0")) {

                JSONObject contexts = resObj.getJSONObject("content");
                String resSign = contexts.getString("sign");
                contexts.remove("sign");
                SortedMap resMap = XXPayUtil.JSONObjectToSortedMap(contexts);
                String reSignStr = XXPayUtil.mapToStringByObj(resMap) + "&secretKey=" + channelPayConfig.getmD5Key();
                String mysign = PayDigestUtil.md5(reSignStr, "UTF-8");
                //核对验签
                if (!resSign.equals(mysign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
                if (contexts.getString("status").equals("2")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
                String status = contexts.getString("status");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                retObj.put("msg", "查询失败,响应Code:" + code + ",响应信息:" + msg + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

            }
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
            case "0":
                return "未支付";
            case "2":
                return "支付成功";
            case "3":
                return "支付失败";
            default:
                return "订单状态未知";
        }
    }
}
