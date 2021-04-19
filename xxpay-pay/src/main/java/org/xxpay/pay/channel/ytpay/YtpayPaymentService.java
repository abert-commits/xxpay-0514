package org.xxpay.pay.channel.ytpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
public class YtpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YtpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YT;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
                //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doYIbPayReq(payOrder, "zfb_yuansheng");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doYIbPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【YT支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("fxid", channelPayConfig.getMchId());//商户号
            map.put("fxorderid", payOrder.getPayOrderId());//订单号
            map.put("fxamount", String.valueOf(amount));// 金额
            map.put("fxnotifyurl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("fxpaytype", pay_code); //银行编码
            map.put("fxretype", "0");//客户端ip
            map.put("fxip", "127.0.0.1");//请求使用编码格式

            String sign = PayDigestUtil.md5(channelPayConfig.getMchId()+payOrder.getPayOrderId()+String.valueOf(amount)
                    +payConfig.getNotifyUrl(channelName)+channelPayConfig.getmD5Key(), "UTF-8").toLowerCase();
            map.put("fxsign", sign);//
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String payUrl = XXPayUtil.buildRequestHtml(map, channelPayConfig.getReqUrl());
            _log.info(logPrefix + "******************上游返回数据:{}", sendMsg);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            JSONObject payParams = new JSONObject();
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl", payUrl);
            payInfo.put("payParams", payParams);
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
        String logPrefix = "【YT支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("fxid", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("fxorderid", payOrder.getPayOrderId()); //商户系统内部的订单号,32个字符内、可包含字母
            map.put("fxaction", "depositquery"); //商户动作

            String sign = PayDigestUtil.md5(channelPayConfig.getMchId() + payOrder.getPayOrderId()+map.get("depositquery")+channelPayConfig.getmD5Key(), "UTF-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("fxsign", sign);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/query", null);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (Integer.parseInt(resObj.getString("code")) != 0) {
                retObj.put("status", "1");
                retObj.put("msg", "上游查询失败");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String return_code = resObj.getJSONObject("message").getString("state");
            if ("1".equals(return_code) || "2".equals(return_code)) {
                retObj.put("status", "2");
                retObj.put("msg", "支付成功");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
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
