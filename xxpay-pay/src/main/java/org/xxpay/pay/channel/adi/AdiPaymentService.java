package org.xxpay.pay.channel.adi;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class AdiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(AdiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ADI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJiandanPayReq(payOrder, "app");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJiandanPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【阿弟付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId());// 商家号
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", f1); //支付金额 元
            if (StringUtils.isNotEmpty(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
//            map.put("youxun_shop", channel); //通道代码

            map.put("callback_url", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("success_url", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            map.put("error_url", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            map.put("out_uid", RandomStringUtils.randomAlphanumeric(31));//随机数
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号
            map.put("version", "v1.1"); //支付产品ID
            map.put("return_type",channel);
            String sign=PayDigestUtil.getSign(map,channelPayConfig.getmD5Key());

            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/sh-alipay-gateway/gateway/api/unifiedorder.do?format=json", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);

            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("200".equals(resultCode)) {
                String payJumpUrl = resObj.getString("url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);

                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("id"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败["+retMsg+"]");
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
        String logPrefix = "【阿弟付支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId());// 商家号
            map.put("out_trade_no", payOrder.getPayOrderId()); //外部订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/sh-alipay-gateway/gateway/api/payTradeQuery.do?format=json", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("200")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }


}
