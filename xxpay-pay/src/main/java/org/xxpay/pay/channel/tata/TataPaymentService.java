package org.xxpay.pay.channel.tata;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.mq.rocketmq.normal.Mq5PayQuery;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class TataPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(TataPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAPAY;
    }

    @Autowired
    private Mq5PayQuery mq5PayQuery;

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doWawaPayReq(payOrder, "0");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doWawaPayReq(payOrder, "1");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doWawaPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【塔塔油卡支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap<>();

            map.put("price", new BigDecimal(payOrder.getAmount()).divide(new BigDecimal(100)).toString());
            map.put("paytype", channel);
            map.put("suborder", payOrder.getPayOrderId());
            String sendMsg = MessageFormat.format("price={0}&paytype={1}&suborder={2}", new BigDecimal(payOrder.getAmount()).divide(new BigDecimal(100)).toString(),
                    channel, payOrder.getPayOrderId());
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject resObj = null;
            String resultCode = "";
            String retMsg = "";
            String res = "";

            res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/recharge?" + sendMsg, "", 30000);
            _log.info(logPrefix + "******************上游返回数据:{}-{}", payOrder.getPayOrderId(), res);
            resObj = JSONObject.parseObject(res);
            resultCode = resObj.getString("error");
            retMsg = resObj.getString("message");

            if (!resultCode.equals("0")) {
                _log.error("下单失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject data = resObj.getJSONObject("data");
            String p_orderid = data.getString("orderid");
            String paydata = data.getString("paydata");
            String sdkdata = data.getString("sdkdata");
            String payJumpUrl = "";
            if (StringUtils.isNotBlank(paydata)) {
                payJumpUrl = paydata;
            }
            if (StringUtils.isNotBlank(sdkdata)) {
                payJumpUrl = sdkdata;
            }

            payJumpUrl = new String(Base64Utils.decode(payJumpUrl));
            String phone = data.getString("login_phone");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            if (payJumpUrl.contains("<form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else if (payJumpUrl.contains("alipay.trade.app.pay")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), phone + "-" + p_orderid);
            if (result > 0) {
                String channelId = payOrder.getChannelId();
                String channelName = channelId.substring(0, channelId.indexOf("_"));
                //调用消息队列查询该订单是否有支付成功
                JSONObject object = new JSONObject();
                object.put("payOrderId", payOrder.getPayOrderId());
                object.put("channelName", channelName);
                mq5PayQuery.Send(object.toJSONString(), 20 * 1000);
            }

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
     * 塔塔油卡查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【塔塔油卡支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            if (StringUtils.isBlank(payOrder.getChannelOrderNo())) {
                retObj.put("status", "1");
                retObj.put("msg", "上游订单创建未成功,支付失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }


            String sendMsg = MessageFormat.format("phone={0}&orderid={1}", payOrder.getChannelOrderNo().split("-")[0], payOrder.getChannelOrderNo().split("-")[1]);
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/orderdetal?" + sendMsg, "");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("error");
            String retMsg = resObj.getString("message");
            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败，上游返回信息:" + retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            retObj.put("msg", "状态不明");
            if (StringUtils.isEmpty(retMsg)) {
                retObj.put("status", "1");
                retObj.put("msg", "未支付");
            } else if (retMsg.contains("成功") || retMsg.contains("已完成") || retMsg.contains("已支付")) {
                retObj.put("status", "2");
                retObj.put("msg", "已支付");
            } else if (StringUtils.isNotBlank(retMsg)) {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
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
}
