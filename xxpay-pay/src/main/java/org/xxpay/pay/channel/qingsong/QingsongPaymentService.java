package org.xxpay.pay.channel.qingsong;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class QingsongPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QingsongPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QINGSONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doFeiBaoPayReq(payOrder, "");//ALISCAN 支付宝扫码支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doFeiBaoPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【轻松医疗码统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); // 支付金额
            map.put("outMchntId", channelPayConfig.getMchId()); // 商户号
            map.put("custSeq", payOrder.getPayOrderId()); // 商户平台订单号
            map.put("remark", ""); // 订单备注
            map.put("gateway_id", channelPayConfig.getChannelId()); // 支付通道编码
            map.put("return_url", payConfig.getNotifyUrl(getChannelName())); // 异步回调地址
            map.put("extend", ""); // 附加参数

            String reString = XXPayUtil.mapToStringEmity(map);
            reString = java.net.URLEncoder.encode(reString,"utf-8");
            reString = reString.replace("%3D", "=").replace("%26", "&");

            String sign = PayDigestUtil.md5(PayDigestUtil.md5(reString, "utf-8").toLowerCase()+channelPayConfig.getmD5Key(), "utf-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); // 签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/index/dopay", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("state");
            JSONObject resultData = resObj.getJSONObject("data");
            if ("1".equals(resultCode) && "1".equals(resultData.getString("payStatus"))) {
                String payJumpUrl = resultData.getString("payUrl");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl",payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("<form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_sn"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error("下单失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败");
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
        String logPrefix = "【轻松医疗码订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            retObj.put("errDes", "操作成功!");
            retObj.put("msg", "查询系统：上游没有查询接口！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

//    public static String http_build_query(Map<String,Object> array){
//        String reString = "";
//        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
//        Iterator it = array.entrySet().iterator();
//        while (it.hasNext()){
//            Map.Entry<String,Object> entry =(Map.Entry) it.next();
//            String key = entry.getKey();
//            Object value = entry.getValue();
//            reString += key+"="+value+"&";
//        }
//        reString = reString.substring(0, reString.length()-1);
//        //将得到的字符串进行处理得到目标格式的字符串
//        try {
//            reString = java.net.URLEncoder.encode(reString,"utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        reString = reString.replace("%3D", "=").replace("%26", "&");
//        return reString;
//    }
}
