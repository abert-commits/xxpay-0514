package org.xxpay.pay.channel.zhongkongpay;//ZkongPaymentService


import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ZhongkongPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ZhongkongPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ZHONGKONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝WAP
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doZhongkongPayReq(payOrder, "qrcode_bank_alipay");
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doZhongkongPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【Zhongkong付支付统一下单】";
        System.out.println("支付名字"+getChannelName());
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("app_key", channelPayConfig.getMchId());//商户key
            map.put("money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));// 单位 元
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName()));
            map.put("out_trade_sn", payOrder.getPayOrderId());//商户订单号
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//10位时间戳

            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(signStr + "&"+channelPayConfig.getmD5Key(), "utf-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            map.put("pay_type", channelPayConfig.getChannelId());//支付渠道
//            map.put("method_type", "json");
            System.out.println(map.toString());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

//            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String reqHtml= XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/order/api/pay/"+ payOrder.getPayOrderId());
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/order/api/pay/"+ payOrder.getPayOrderId(),sendMsg);
//            _log.info(logPrefix + "******************上游返回数据:{}", res);
//            JSONObject resObj = JSONObject.parseObject(res);
//
//
//
//                String payJumpUrl = res;
//                _log.info(logPrefix+"请求返回字段  data里面的数据 ：{}",payJumpUrl);
//
//                payJumpUrl = JSONObject.parseObject(payJumpUrl).getString("pay_data");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", reqHtml);
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
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
        String logPrefix = "【Zhongkong支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("app_key", channelPayConfig.getMchId());//商户编号
            map.put("out_trade_sn", payOrder.getPayOrderId());//訂單號

            String sign =PayDigestUtil.md5(channelPayConfig.getMchId()+payOrder.getPayOrderId()+channelPayConfig.getmD5Key(), "utf-8").toUpperCase();//PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/order/api/check/order", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject data = JSONObject.parseObject(resObj.getString("data"));
            if (retCode.equals("200")) {
                String status = data.getString("status");
                retObj.put("status", 2);
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(status) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String  code) {
        switch (code) {
            case "0":
                return "未支付";
            case "1":
                return "已支付";
            case "2":
                return "已过期";
            default:
                return "状态不明";
        }
    }
}

