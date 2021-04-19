package org.xxpay.pay.channel.dashenxin;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashenxinPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DashenxinPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_KUAICAITONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doHuaFeiPayReq(payOrder, "1005"); //pay.unionpay.scan银联扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【新大神支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            if (StringUtils.isNotBlank(channelPayConfig.getChannelId())) {
                channel = channelPayConfig.getChannelId();
            }
            LinkedHashMap map = new LinkedHashMap();
            map.put("parter", channelPayConfig.getMchId());// 商家号
            map.put("type", channel);// 银行类型	 类型
            map.put("value", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //金额 元
            map.put("orderid", payOrder.getPayOrderId());// 商户订单号
            map.put("vsion","1");
            map.put("callbackurl",payConfig.getNotifyUrl(channelName));// 下行异步通知地址
            String mapToString = "value="+map.get("value")+"&parter="+map.get("parter")+"&type="+map.get("type")+"&orderid="+map.get("orderid")+"&callbackurl="+map.get("callbackurl")+channelPayConfig.getmD5Key();
            _log.info(logPrefix + "******************mapToString:{}", mapToString);

            String sign=PayDigestUtil.md5(mapToString,"UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            map.put("agent", ""); //代理ID


            String res = XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/interface/chargebank.aspx");
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            if (res.contains("html")) {
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", res);
                if (res.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + null + "]");
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
     //* @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【快财通支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            LinkedHashMap map = new LinkedHashMap();
            map.put("parter", channelPayConfig.getMchId());// 商家号
            map.put("orderid", payOrder.getPayOrderId()); //商户订单号
            map.put("type","1");

            String mapToString = "parter="+map.get("parter")+"&type="+map.get("type")+"&orderid="+map.get("orderid")+channelPayConfig.getmD5Key();
            _log.info(logPrefix + "******************mapToString:{}", mapToString);

            String sign=PayDigestUtil.md5(mapToString,"UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/Query.aspx", sendMsg);
            _log.info("上游返回信息：" + res);
            Map<String,String> map1 = XXPayUtil.convertParamsString2Map(res);
            String opstate = map1.get("opstate");
            if (opstate.equals("0")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + opstate + ",订单状态:" + GetStatusMsg(opstate) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "支付成功";
            case "1":
                return "商户订单号无效";
            case "2":
                return "签名错误";
            case "3":
                return "请求参数无效";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
