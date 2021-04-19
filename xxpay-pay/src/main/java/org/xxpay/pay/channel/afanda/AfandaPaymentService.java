package org.xxpay.pay.channel.afanda;

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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class AfandaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(AfandaPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_AFANDA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doAFanDaPayReq(payOrder, "ap"); //pay.alipay.h5 支付宝 SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doAFanDaPayReq(payOrder, "ap"); //支付宝 h5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doAFanDaPayReq(payOrder, "ap"); //支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doAFanDaPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【阿凡达支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            map.put("merchant", channelPayConfig.getMchId());// 商家号
            map.put("qrtype", channel);//支付通道
            map.put("customno", payOrder.getPayOrderId()); //商户订单号
            map.put("money", amount); //支付金额
            map.put("sendtime", String.valueOf(System.currentTimeMillis()/1000L));
            map.put("notifyurl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("backurl", "");
            map.put("risklevel", "");

            String str = String.format("merchant=%s&qrtype=%s&customno=%s&money=%s&sendtime=%s&notifyurl=%s&backurl=%s&risklevel=%s",
                    map.get("merchant"), map.get("qrtype"), map.get("customno"), map.get("money"), map.get("sendtime"), map.get("notifyurl"), map.get("backurl"), map.get("risklevel"));
            String sign = PayDigestUtil.md5((str+channelPayConfig.getmD5Key()), "UTF-8").toLowerCase();

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/v3/cashier.php", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            if (res.contains("form")) {
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", res);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (res.contains("form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                JSONObject resObj = JSONObject.parseObject(res);
                String msg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败[" + msg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
//
//
//
//            JSONObject resObj = JSONObject.parseObject(res);
//            String resultCode = resObj.getString("retCode");
//            String retMsg = resObj.getString("retMsg");
//
//            if ("SUCCESS".equals(resultCode)) {
//                String signs = resObj.getString("sign");
//                resObj.remove("sign");
//                // md5 加密
//                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
//                if (md5.equals(signs)) {
//                    String payJumpUrl = resObj.getJSONObject("payParams").getString("payUrl");
//                    JSONObject payParams = new JSONObject();
//                    if (channelPayConfig.getRsaprivateKey()!=null && channelPayConfig.getRsaprivateKey().equals("1")) {
//                        payParams.put("payJumpUrl", URLDecoder.decode(payJumpUrl, "UTF-8"));
//                    }else{
//                        payParams.put("payJumpUrl", payJumpUrl);
//                    }
//
//                    if (payOrder.getChannelId().contains("SDK")) {
//                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
//                    } else if (payJumpUrl.contains("form")) {
//                        //表单跳转
//                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
//                    } else {
//                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
//                    }
//                    payInfo.put("payParams", payParams);
//                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
//                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
//                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//                } else {
//                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
//                    payInfo.put("errDes", "下单失败[验签失败]");
//                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                    return payInfo;
//                }
//            } else {
//                payInfo.put("errDes", "下单失败[" + retMsg + "]");
//                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//                return payInfo;
//            }
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
        String logPrefix = "【阿凡达支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchant", channelPayConfig.getMchId());// 商家号
            map.put("customno", payOrder.getPayOrderId()); //商户订单号
            map.put("sendtime", String.valueOf(System.currentTimeMillis()/1000L));

            String str = String.format("merchant=%s&customno=%sendtime=%s", map.get("merchant"), map.get("customno"), map.get("sendtime"));
            String sign = PayDigestUtil.md5((str+channelPayConfig.getmD5Key()), "UTF-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/v3/query.php", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("errCode");
            if ("0".equals(retCode)) {
                JSONObject retData = resObj.getJSONObject("data");
                if (retData.getInteger("state") == 1) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retData.getInteger("state")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态: 查询失败");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(int code) {
        switch (code) {
            case 0:
                return "未支付/支付失败";
            case 1:
                return "支付成功";
            case -1:
                return "订单关闭";
            default:
                return "交易失败";
        }
    }
}
