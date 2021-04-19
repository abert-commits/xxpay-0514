package org.xxpay.pay.channel.lvbei;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import sun.security.rsa.RSASignature;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class LvbeiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LvbeiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QUZHIFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doPayReq(payOrder, "");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【绿呗支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳
            map.put("apiKey", channelPayConfig.getmD5Key());//apikey
            map.put("signType", "rsa");//加密類型
            map.put("appId", "HX9527008");
            map.put("outOrderNo", payOrder.getPayOrderId());// 订单号
            map.put("amount", String.valueOf(payOrder.getAmount()));//金额 分
            map.put("channel", channelPayConfig.getChannelId());//支付渠道
            map.put("subject", "HxGoods");
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));

            String content = XXPayUtil.mapToString(map);
            String rsaprivateKey = channelPayConfig.getRsaprivateKey();
            String sign = RSASHA256withRSAUtils.sign(content.getBytes(), rsaprivateKey);
            _log.info(logPrefix + "******************sign:{}", sign);
            sign = sign.replace("+", "*").replace("/", "-").replace("=", ".");
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String formHtml = XXPayUtil.buildRequestHtml(map, channelPayConfig.getReqUrl() + "/v1/cashiers/h5/orders");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", formHtml);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
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
        String logPrefix = "【绿呗支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳
            map.put("apiKey", channelPayConfig.getmD5Key());//apikey
            map.put("signType", "rsa");//加密類型

            map.put("appId", "HX9527008");//APPID
            map.put("outOrderNo", payOrder.getPayOrderId());//订单号


            String content = XXPayUtil.mapToString(map);
            String rsaprivateKey = channelPayConfig.getRsaprivateKey();
            String sign = RSASHA256withRSAUtils.sign(content.getBytes(), rsaprivateKey);
            _log.info(logPrefix + "******************sign:{}", sign);
            sign = sign.replace("+", "*").replace("/", "-").replace("=", ".");
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/v1/invoices?" + sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String status=resObj.getString("status");
            String amount=resObj.getString("amount");
            if (!String.valueOf(payOrder.getAmount()).equals(amount))
            {
                retObj.put("status", "1");
                retObj.put("msg", "上游返回订单金额不符！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            if (status.equals("2"))
            {
                retObj.put("status", "2");
                retObj.put("msg", "支付成功");
            }else {
                retObj.put("status", "1");
                retObj.put("msg", "未支付");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
