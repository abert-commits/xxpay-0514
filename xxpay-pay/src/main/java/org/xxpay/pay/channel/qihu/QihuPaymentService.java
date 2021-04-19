package org.xxpay.pay.channel.qihu;

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
public class QihuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QihuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMayiPayReq(payOrder, "alipay");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "wx");
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【七虎支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "1.0.0");//版本号
            map.put("token", channelPayConfig.getMchId());//用户令牌，商户号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//金额 元
            map.put("product_name", "Goods");
            map.put("paycode", channelPayConfig.getChannelId());//通道编码
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            map.put("attach", "HX_Show");


            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("front_url", payConfig.getNotifyUrl(channelName));
            map.put("asynch_url", payConfig.getNotifyUrl(channelName));
            map.put("data_type", "2");
            String strSign = XXPayUtil.mapToString(map) + "&secret_key=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(strSign, "UTF-8");

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            String payUrl = XXPayUtil.buildRequestHtml(map, channelPayConfig.getReqUrl() + "/gateway.do");
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway.do", sendMsg);
            JSONObject object = JSONObject.parseObject(res);
            String code = object.getString("code");
            String msg = object.getString("msg");
            if (!code.equals("1")) {
                payInfo.put("errDes", "下单失败，失败原因：" + msg);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            JSONObject data = object.getJSONObject("data");
            String out_trade_no = data.getString("out_trade_no");
            String payUrl = data.getString("qrcode");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payUrl);
            payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
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
        String logPrefix = "【七虎支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "1.0.0");//商户编号
            map.put("token", channelPayConfig.getMchId());//商户号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//时间戳
//            map.put("orderid",payOrder.getPayOrderId());//订单号
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号

            String strSign = XXPayUtil.mapToString(map) + "&secret_key=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(strSign, "UTF-8");
            map.put("sign", sign);
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/query.do", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (!retCode.equals("1")) {
                retObj.put("status", "1");
                retObj.put("msg", retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            JSONObject data = resObj.getJSONObject("data");
            String state = data.getString("pay_status");
            if (state.equals("1")) {
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
