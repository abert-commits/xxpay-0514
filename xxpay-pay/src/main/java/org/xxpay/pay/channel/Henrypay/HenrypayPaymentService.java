package org.xxpay.pay.channel.Henrypay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

@Service
public class HenrypayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HenrypayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    public static long tokenDate=0;

    public static long expireTime = 0;

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HENRY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doHenRyPayReq(payOrder, "/api/v1/order/wechatScan");//微信扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHenRyPayReq(payOrder, "/api/v1/order/wechatWapPay"); //微信H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHenRyPayReq(payOrder, "/api/v1/order/alipayScan"); //支付宝扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHenRyPayReq(payOrder, "/api/v1/order/alipayWapPay"); // 支付宝H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHenRyPayReq(payOrder, "/api/v1/order/unionpayScan"); //支付宝SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHenRyPayReq(PayOrder payOrder, String url) {
        String logPrefix = "【henry支付统一下单】";
        JSONObject payInfo = new JSONObject();
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

        try {
            //判断是否 有token  和 tokenDate
            if ("".equals(accessToken.toString()) && tokenDate == 0) {
                if (!getAccessToken(channelPayConfig)) {
                    _log.error("获取getAccessToken失败，payOrderId={}", payOrder.getPayOrderId());
                    payInfo.put("errDes", "下单失败[获取getAccessToken失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            }
            //判断accesstoken 是否 超时
            long newDate = new Date().getTime();
            if ((newDate - tokenDate) / 1000 > expireTime) {
                // 超时重新获取accesstoken
                if (!getAccessToken(channelPayConfig)) {
                    _log.error("获取getAccessToken失败，payOrderId={}", payOrder.getPayOrderId());
                    payInfo.put("errDes", "下单失败[获取getAccessToken失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            }

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("accessToken", accessToken.toString());// 商家号

            Map map = new HashMap();
            map.put("outTradeNo", payOrder.getPayOrderId()); //商户唯一订单号
            map.put("money", payOrder.getAmount()); //商户订单金额
            map.put("type", "T1"); //付款类型
            map.put("body", "韩信支付"); //韩信支付
            map.put("detail", "韩信支付"); //商品详情
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //异步通知地址
            if ("/api/v1/order/wechatWapPay".equals(url)) {
                map.put("merchantIp", "127.0.0.1"); //客户端IP
            }
            map.put("productId", "hanxinpay"); //商品ID
            jsonObject.put("param", map);// 商家号
            _log.info(logPrefix + "******************accessToken:{}",  accessToken.toString());
            _log.info(logPrefix + "******************sendMsg:{}", jsonObject.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + url, jsonObject.toJSONString());
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            boolean success = resObj.getBoolean("success");
            String retMsg = resObj.getString("message");
            if (success) {
                    String payJumpUrl = resObj.getString("value");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    //表单跳转
                    if (payJumpUrl.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
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
     * 获取accessToken
     *
     * @param channelPayConfig
     */
    private boolean getAccessToken(ChannelPayConfig channelPayConfig) {
        String logPrefix = "【Henry支付订单获取access_token】";

        JSONObject map = new JSONObject();
        map.put("merchantNo", channelPayConfig.getMchId());// 商家号
        map.put("nonce", RandomStringUtils.randomAlphanumeric(31)); //随机码
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); // 时间戳(10位秒)
        String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
        map.put("sign", sign);
        _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());

        String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/v1/getAccessToken/merchant", map.toJSONString());
        _log.info("上游返回信息：" + res);
        JSONObject resObj = JSONObject.parseObject(res);
        boolean success = resObj.getBoolean("success");
        if (success) {
            accessToken=new StringBuffer();
            expireTime = resObj.getJSONObject("value").getInteger("expireTime") - 200;
            accessToken.append(resObj.getJSONObject("value").getString("accessToken"));
            tokenDate = new Date().getTime();
        }
        return success;
    }


    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【Henry支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("merchant_code", channelPayConfig.getMchId());// 商家号
            map.put("order_no", String.valueOf(payOrder.getPayOrderId())); // 商户订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/gateway/query",  map.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            if (code.equals("00")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("code", code);//商户编号
                map1.put("merchant_code", resObj.getString("merchant_code"));
                map1.put("order_no", resObj.getString("order_no"));
                map1.put("amount", resObj.getString("amount"));
                map1.put("status", resObj.getString("status"));

                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                } else {
                    //支付成功
                    retObj.put("status", "2");
                }
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(resObj.getString("status")) + "");
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
            case "00":
                return "支付成功";
            case "11":
                return "未支付";
            case "99":
                return "交易关闭";
            default:
                return "交易失败";
        }
    }
}
