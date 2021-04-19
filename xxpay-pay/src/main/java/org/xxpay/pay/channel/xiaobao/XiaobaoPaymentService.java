package org.xxpay.pay.channel.xiaobao;

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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class XiaobaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XiaobaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JJPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = jjPyaRyPayReq(payOrder);
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject jjPyaRyPayReq(PayOrder payOrder) {
        String logPrefix = "【小爆脾气支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new LinkedHashMap();
            map.put("MerchantID", channelPayConfig.getMchId());
            map.put("PayID", "1");
            map.put("OrderNo", channelPayConfig.getMchId() + payOrder.getPayOrderId());
            map.put("Money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));
            map.put("BankType", channelPayConfig.getChannelId()); //银行编码
            map.put("PayDesc", "HX_Shopping");
//            map.put("Ip", "92.97.124.162");//客户端ip
            map.put("Ip", payOrder.getClientIp());//客户端ip
            map.put("NotifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("ShowUrl", payConfig.getReturnUrl(channelName));// 同步跳转地址
            String signStr = XXPayUtil.mapToString(map).replace(">", "") + "&MerchantKey=" + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            map.put("Sign", sign.toUpperCase());// md5签名
            map.put("TenPayID", map.get("PayID"));// md5签名
            map.put("Desc", map.get("PayDesc"));// md5签名
            map.remove("PayID");
            map.remove("PayDesc");
            _log.info(logPrefix + "******************sign:{}", sign);
            String formHtml = XXPayUtil.buildRequestHtml(map, channelPayConfig.getReqUrl() + "/api/WapAliPayOnlineSubmit.aspx");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", formHtml);
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
        String logPrefix = "【吉吉支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("agentNo", channelPayConfig.getMchId());//商户号
            map.put("orderNo", payOrder.getPayOrderId());//订单号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//随机串

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/orderQuery", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String returnCode = resObj.getString("returnCode");
            String returnMsg = resObj.getString("returnMsg");
            if (resObj.getString("success").equals("true")) {
                SortedMap sortedMap = new TreeMap();
                sortedMap.put("orderNo", resObj.getString("orderNo"));
                sortedMap.put("tradeNo", resObj.getString("tradeNo"));
                sortedMap.put("orderStatus", resObj.getString("orderStatus"));
                sortedMap.put("orderAmount", resObj.getString("orderAmount"));
                sortedMap.put("createTime", resObj.getString("createTime"));
                sortedMap.put("orderType", resObj.getString("orderType"));

                String resSign = resObj.getString("sign");
                String checkSignStr = XXPayUtil.mapToString(sortedMap).replace(">", "");
                String checkSign = PayDigestUtil.getSign(checkSignStr, channelPayConfig.getmD5Key()).toLowerCase();
                if (!checkSign.equals(resSign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游查询同步返回,验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                    return retObj;
                }
            }
            if (resObj.getString("orderStatus").equals("SUCCESS")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            String status = resObj.getString("orderStatus");
            retObj.put("msg", "响应Code:" + status + ",订单状态:" + GetStatusMsg(status) + "");
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
            case "PROCESSING":
                return "支付中";
            case "SUCCESS":
                return "已支付";
            case "FAIL":
                return "支付失败";
            default:
                return "未支付";
        }
    }

}
