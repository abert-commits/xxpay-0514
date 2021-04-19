package org.xxpay.pay.channel.dashenpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class DashenpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DashenpayPaymentService.class);

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
                retObj = doDashenRyPayReq(payOrder, ""); //支付宝WAP h5 jfali,jfwx可填任意值aliwap
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDashenRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【大神支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            SortedMap map = new TreeMap();
            map.put("merch_id", channelPayConfig.getMchId());//商户号
            map.put("product", channelPayConfig.getChannelId());//支付类型
            map.put("order_id", payOrder.getPayOrderId());// 商户订单号
            map.put("amount", String.valueOf(amount));// 金额
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/index.php/trade/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("success")) {
                JSONObject payParams = new JSONObject();
                String payJumpUrl = resObj.getString("pay_url");
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
        String logPrefix = "【大神支付订单查询】";
        JSONObject jsonObject = new JSONObject();
        JSONObject retObj = new JSONObject();
        byte[] context = null;
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            jsonObject.put("orderNo", payOrder.getPayOrderId());// 订单号
            jsonObject.put("merchNo", channelPayConfig.getMchId()); // 商户号
            context = JSON.toJSONString(jsonObject).getBytes("UTF-8");
            String sign = PayDigestUtil.md5(new String(getContentBytes(new String(context, "UTF-8") + channelPayConfig.getmD5Key(), "UTF-8"), "UTF-8"), "UTF-8");
            JSONObject jo = new JSONObject();
            jo.put("sign", sign);
            jo.put("context", context);
            jo.put("encryptType", "MD5");
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/order/query", jo.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            ChannelPayConfig channelPayConfigs = new ChannelPayConfig(getPayParam(payOrder));
            if (code.equals("0")) {
                String signs = resObj.getString("sign");
                String contexts = resObj.getString("context");
                String json1 = new String(Base64Utils.decode(contexts.getBytes("UTF-8")), "UTF-8");
                ChannelPayConfig channelPayConfig1 = new ChannelPayConfig(getPayParam(payOrder));
                String rsign = PayDigestUtil.md5(new String(DashenpayPaymentService.getContentBytes(json1 + channelPayConfig1.getmD5Key(), "UTF-8"), "UTF-8"), "UTF-8");
                //核对验签
                if (!signs.equals(rsign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
                if (resObj.getString("code").equals("0")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
                String status = resObj.getString("code");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
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
                return "支付成功";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
