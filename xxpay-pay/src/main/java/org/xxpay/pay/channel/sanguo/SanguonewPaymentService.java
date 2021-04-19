package org.xxpay.pay.channel.sanguo;

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
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SanguonewPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(SanguoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝wap
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }

        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【三国支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("type", channel);//通道代码
            map.put("total", payOrder.getAmount().toString());
            map.put("api_order_sn", payOrder.getPayOrderId());//商户订单号
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notify_url", payConfig.getNotifyUrl(channelName));
            map.put("client_id", channelPayConfig.getMchId());// 商户ID
            map.put("timestamp", DateUtil.getRevTime());//时间戳
            String signStr = channelPayConfig.getmD5Key() + XXPayUtil.mapToStringValue(map) + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
//            map.put("sign_type", "MD5");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/api/order", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (!resultCode.equals("200"))
            {
                payInfo.put("errDes", "下单失败,失败信息:" + retMsg + "！");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject jsonObject=resObj.getJSONObject("data");
            String chanelOrderId=jsonObject.getString("order_sn");
            String payUrl=jsonObject.getString("sdk");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payUrl);
            payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), chanelOrderId);
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
        String logPrefix = "【三国支付订单查询】";
        JSONObject retObj = new JSONObject();
        //验签失败
        retObj.put("status", "2");
        retObj.put("msg", "上游没有查询接口");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }
}
