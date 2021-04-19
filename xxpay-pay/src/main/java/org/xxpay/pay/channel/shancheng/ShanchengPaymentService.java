package org.xxpay.pay.channel.shancheng;

import com.alibaba.fastjson.JSONObject;
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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ShanchengPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ShanchengPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SHANCHENG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doWuduRyPayReq(payOrder, ""); //支付宝拼多多
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doWuduRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【山城支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("partner", channelPayConfig.getMchId());//商户号
            map.put("payment_type", "2");//商户号
            map.put("total_fee", String.valueOf(amount));// 金额
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            map.put("return_url", payConfig.getReturnUrl(channelName));//同步地址
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("body", "山城支付"); //商品说明
            String str = String.format("partner=%s&out_trade_no=%s&total_fee=%s&payment_type=%s&notify_url=%s&return_url=%s&body=%s",
                    map.get("partner"), map.get("out_trade_no"), map.get("total_fee"), map.get("payment_type"), map.get("notify_url"), map.get("return_url"), map.get("body"));
            String sign = PayDigestUtil.md5((str + channelPayConfig.getmD5Key()), "UTF-8");
            _log.info(sign + "******************sign:{}", sign);
            map.put("sign", sign);// md5签名
            map.put("sign_type ", "MD5"); //请求时间
            map.put("channel", channelPayConfig.getChannelId()); //银行编码
            System.out.println(map.toString());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/appmerchantproxy/transfer");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            if (res.contains("html")) {
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", res);
                if (payOrder.getChannelId().contains("SDK")) {
                    if (res.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    }
                } else {
                    if (res.contains("form")) {
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

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【山城支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        retObj.put("errDes", "操作失败!");
        retObj.put("msg", "查询系统：请求上游无查询通道！");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        return retObj;

//        try {
//            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
//            SortedMap map = new TreeMap();
//            map.put("uid", channelPayConfig.getMchId());//商户号
//            map.put("order_no", payOrder.getPayOrderId());//订单号
//            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
//            map.put("sign",sign);
//            _log.info(logPrefix + "******************sign:{}", sign);
//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Apipay_Trade_query.html",sendMsg);
//            _log.info("上游返回信息：" + res);
//            JSONObject resObj = JSONObject.parseObject(res);
//            String code = resObj.getString("message");
//            if (code.equals("CODE_SUCCESS")) {
//                String ressign = resObj.getString("sign");
//                //验签
//                SortedMap map1 = new TreeMap();
//                map1.put("uid", resObj.getString("uid"));//商户编号
//                map1.put("order_no", resObj.getString("order_no"));//平台订单号
//                map1.put("amount", resObj.getString("amount"));//订单金额
//                map1.put("time", resObj.getString("time"));//支付成功时间
//                map1.put("platform_order_no", resObj.getString("platform_order_no"));//交易流水号
//                map1.put("status", resObj.getString("status"));//交易状态
//                map1.put("message", resObj.getString("message"));//交易状态
//                System.out.println("山城------》》》》》》《《《《《《++++++" + map1.toString());
//                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
//                //核对验签
//                if (!signMd5.equals(ressign)) {
//                    retObj.put("status", "1");
//                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
//                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//                }
//            }
//            if (resObj.getString("status").equals("1")) {
//                retObj.put("status", "2");
//            } else {
//                retObj.put("status", "1");
//            }
//
//            String status=resObj.getString("status");
//            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            return retObj;
//        } catch (Exception e) {
//            retObj.put("errDes", "操作失败!");
//            retObj.put("msg", "查询系统：请求上游发生异常！");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//            return retObj;
//        }
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
