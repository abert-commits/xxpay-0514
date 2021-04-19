package org.xxpay.pay.channel.qingtian;

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
import java.text.MessageFormat;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QingtianPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QingtianPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_")+1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【晴天支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("customerid", channelPayConfig.getMchId());//商户编号
            map.put("sdorderno", payOrder.getPayOrderId());//商户订单号

            BigDecimal b   =   new   BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).toString();
            map.put("total_fee", String.valueOf(f1));// 单位 元
            map.put("paytype",channel);//支付編碼
            map.put("notifyurl", payConfig.getNotifyUrl(getChannelName()));
            map.put("returnurl", payConfig.getNotifyUrl(getChannelName()));
            map.put("remark","商品");
            String signStr= MessageFormat.format("customerid={0}&sdorderno={1}&total_fee={2}&remark={3}&paytype={4}&notifyurl={5}&returnurl={6}&{7}",
                    map.get("customerid"), map.get("sdorderno"), map.get("total_fee"),map.get("remark"),map.get("paytype"),map.get("notifyurl"),map.get("returnurl"),channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr,"UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl(), sendMsg);
            _log.info("上游返回信息："+res);
            JSONObject resObj = JSONObject.parseObject(res);
            Integer retCode = resObj.getInteger("status");
            String retMsg = resObj.getString("msg_info");
            if (retCode == 1) {
                JSONObject payParams = new JSONObject();

                String payJumpUrl = resObj.getString("h5_url");
                String orderid =resObj.getString("orderid");

                payParams.put("payJumpUrl", payJumpUrl);
                //表单跳转
                if(payJumpUrl.contains("form"))
                {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                }else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams",payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), orderid);
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
}
