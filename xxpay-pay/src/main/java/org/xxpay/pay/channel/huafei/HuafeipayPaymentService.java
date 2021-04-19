package org.xxpay.pay.channel.huafei;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class HuafeipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HuafeipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUAFEI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doHuaFeiPayReq(payOrder, "pay.weixin.scan");//pay.weixin.scan微信扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHuaFeiPayReq(payOrder, "pay.weixin.h5"); //pay.weixin.h5 微信H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuaFeiPayReq(payOrder, "pay.alipay.scan"); //pay.alipay.scan支付宝扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "pay.alipay.h5"); //pay.alipay.h5 支付宝H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuaFeiAppPayReq(payOrder, "pay.alipay.app"); //pay.alipay.h5 支付宝 SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPayQR:
                retObj = doHuaFeiPayReq(payOrder, "pay.unionpay.scan"); //pay.unionpay.scan银联扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doHuaFeiPayReq(payOrder, "pay.unionpay.h5"); //pay.unionpay.h5 银联H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_Express:
                retObj = doHuaFeiPayReq(payOrder, "pay.express"); //pay.express快捷支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【华东支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            if (!StringUtil.isBlank(channelPayConfig.getChannelId())) {
                channel = channelPayConfig.getChannelId();
            }
            map.put("merchant_code", channelPayConfig.getMchId());// 商家号
            map.put("service_type", channel); //业务类型

            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //异步通知地址
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); //同步返回地址
            map.put("client_ip", "127.0.0.1"); //客户端IP
            map.put("order_no", payOrder.getPayOrderId()); //商户唯一订单号
            map.put("order_time", DateUtil.getSeqString()); //商户订单时间

            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", f1); //商户订单金额

            map.put("coin_type", "CNY"); //币种
            map.put("product_name", "hanxinpay"); //商品名称
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/request_payurl/", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            if ("00".equals(resultCode)) {
                SortedMap mapJM = new TreeMap();
                mapJM.put("code", resObj.getString("code"));//是否成功
                mapJM.put("merchant_code", resObj.getString("merchant_code"));//商家号
                mapJM.put("order_no", resObj.getString("order_no"));//商户网站订单号
                mapJM.put("service_type", resObj.getString("service_type"));//交易类型
                mapJM.put("amount",resObj.getString("amount"));//交易金额
                mapJM.put("pay_url",resObj.getString("pay_url"));//支付页面
                // md5 加密
                String md5 = PayDigestUtil.getSign(mapJM, channelPayConfig.getmD5Key());
                if(md5.equals(resObj.getString("sign"))){
                    String payJumpUrl = resObj.getString("pay_url");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    //表单跳转
                    if(payJumpUrl.contains("form"))
                    {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    }else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams",payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }else{
                    _log.error("验签失败，payOrderId={},res={}",payOrder.getPayOrderId(),res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                String retMsg = resObj.getString("msg");
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
     * 华东 sdk
     * @param payOrder
     * @param channel
     * @return
     */
    public JSONObject doHuaFeiAppPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【华东支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            map.put("merchant_code", channelPayConfig.getMchId());// 商家号
            map.put("service_type", channel); //业务类型

            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //异步通知地址
            map.put("return_url", payConfig.getReturnUrl(getChannelName())); //同步返回地址
            map.put("client_ip", "127.0.0.1"); //客户端IP
            map.put("order_no", payOrder.getPayOrderId()); //商户唯一订单号
            map.put("order_time", DateUtil.getSeqString()); //商户订单时间

            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", f1); //商户订单金额

            map.put("coin_type", "CNY"); //币种
            map.put("product_name", "hanxinpay"); //商品名称
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/request_app_pay/", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            if ("00".equals(resultCode)) {
                SortedMap mapJM = new TreeMap();
                mapJM.put("code", resObj.getString("code"));//是否成功
                mapJM.put("merchant_code", resObj.getString("merchant_code"));//商家号
                mapJM.put("order_no", resObj.getString("order_no"));//商户网站订单号
                mapJM.put("service_type", resObj.getString("service_type"));//交易类型
                mapJM.put("app_call_content", resObj.getString("app_call_content"));//支付页面
                mapJM.put("sdkfrom",resObj.getString("sdkfrom"));//交易类型

                // md5 加密
                String md5 = PayDigestUtil.getSign(mapJM, channelPayConfig.getmD5Key());
                if(md5.equals(resObj.getString("sign"))){
                    String payJumpUrl = URLDecoder.decode(resObj.getString("app_call_content"),"UTF-8");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    payInfo.put("payParams",payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }else{
                    _log.error("验签失败，payOrderId={},res={}",payOrder.getPayOrderId(),res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                String retMsg = resObj.getString("msg");
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【华东支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchant_code", channelPayConfig.getMchId());// 商家号
            map.put("order_no", String.valueOf(payOrder.getPayOrderId())); // 商户订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/query", sendMsg);
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
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(resObj.getString("status"))+ "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private  String GetStatusMsg(String code)
    {
        switch (code)
        {
            case  "00": return  "支付成功";
            case  "11": return  "未支付";
            case  "99": return  "交易关闭";
            default: return  "交易失败";
        }
    }
}
