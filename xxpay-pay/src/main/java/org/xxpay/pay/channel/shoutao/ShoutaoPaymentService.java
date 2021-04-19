package org.xxpay.pay.channel.shoutao;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ShoutaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ShoutaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XUNFEI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝话费H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDD:
                retObj = doMayiPayReq(payOrder, "978");
                break;
            //支付宝原生H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "924");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【v8手淘支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }


            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户编号
            map.put("pay_orderid", payOrder.getPayOrderId());//商户订单号
            map.put("pay_applydate", DateUtil.date2Str(new Date()));//订单日期
            map.put("pay_bankcode", channel);//支付渠道
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_notifyurl", payConfig.getNotifyUrl(channelName));
            map.put("pay_callbackurl", "127.0.0.1");
            map.put("pay_amount", String.valueOf(amount));// 单位 元
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign.toUpperCase());
            map.put("ctoken", RandomStringUtils.randomAlphanumeric(32));//随机字符串
            map.put("pay_productname", "韩信");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Index.html", sendMsg);
            _log.info("上游返回信息：" + res);
            if (res.contains("error_code")) {
                JSONObject resObj = JSONObject.parseObject(res);
                String retMsg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject payParams = new JSONObject();

            if (payOrder.getChannelId().contains("SDK")) {
                 payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (res.contains("form")||res.contains("<html")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }

            payParams.put("payJumpUrl", res);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
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

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【qt支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户编号
            map.put("pay_orderid", payOrder.getPayOrderId());//上游訂單號
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Trade_query.html", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("returncode");

            if (resObj.containsKey("status") && resObj.getString("status").equals("error")) {
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
            if (retCode.equals("00")) {
                // 订单成功
                BigDecimal amount = resObj.getBigDecimal("amount");
                // 核对金额
                long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                if (outPayAmt == outPayAmt) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", GetStatusMsg(resObj.get("trade_state").toString()));
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "查询错误");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "SUCCESS":
                return "已支付";
            default:
                return "未支付";
        }
    }
}
