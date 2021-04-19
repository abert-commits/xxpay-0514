package org.xxpay.pay.channel.qpay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.sunnypay.util.HttpUtil;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QxianjinPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QxianjinPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doQpayPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doQpayPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【Q支付现金红包统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId()); // 商户UID
            map.put("child_type", channelPayConfig.getRsapassWord()); // 设备类型
            map.put("out_trade_no", payOrder.getPayOrderId()); // 订单号
            map.put("pay_type", channelPayConfig.getChannelId()); // 支付方式
            map.put("total_fee", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("notify_url", URLEncoder.encode(payConfig.getNotifyUrl(channelName), "UTF-8")); // 支付结果后台回调URL
            map.put("timestamp", String.valueOf(System.currentTimeMillis()/1000)); // 支付结果后台回调URL
            map.put("mch_secret", channelPayConfig.getmD5Key());

            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(signStr, "UTF-8").toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            map.remove("mch_secret");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/create", sendMsg, "utf-8", "application/x-www-form-urlencoded");
//            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/gateway/createOrder", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");

            if ("100".equals(resultCode)) {
//                String payJumpUrl = URLDecoder.decode(retData.getString("orderStr"), "UTF-8").toLowerCase();
                String payJumpUrl = retData.getString("orderStr");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), retData.getString("order_id"));
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

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【Q支付现金红包订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
//            SortedMap map = new TreeMap();
//            map.put("order_id", payOrder.getChannelOrderNo()); //应用ID

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            String res = HttpClient.callHttpPost(channelPayConfig.getReqUrl() + "/pay/Query" + payOrder.getChannelOrderNo());
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/Query/" + payOrder.getChannelOrderNo(), "");
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retStatus = resObj.getString("status");
            String retCode = resObj.getString("code");
            retObj.put("status", "2");
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retCode) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "-1":
                return "用户取消支付";
            case "0":
                return "订单已创建";
            case "1":
                return "正在支付";
            case "2":
                return "正在回调";
            case "3":
                return "回调失败或处理失败";
            case "4":
                return "回调成功,订单完结";
            default:
                return "异常订单";
        }
    }
}
