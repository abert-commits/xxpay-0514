package org.xxpay.pay.channel.qiangu;

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

import java.net.URLDecoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QianguPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QianguPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QIANGU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuaFeiPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【千古闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("id", channelPayConfig.getMchId()); //应用ID
            map.put("part_sn", payOrder.getPayOrderId());
            map.put("money", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("notify", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL

            String signStr = channelPayConfig.getmD5Key() + "id" + map.get("id") + "money" + map.get("money") + "notify" + map.get("notify") + "part_sn" +
                    map.get("part_sn") + channelPayConfig.getmD5Key();
            _log.info(logPrefix + "******************signStr:{}", signStr);
            String sign = PayDigestUtil.md5(signStr, "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/order", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("1".equals(resultCode)) {
                JSONObject retData = resObj.getJSONObject("data");
                String payJumpUrl = URLDecoder.decode(retData.getString("sdk_url"), "utf-8");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
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
        String logPrefix = "【千古闲鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        retObj.put("status", "2");
        retObj.put("msg", "上游没有查询接口");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
//        try {
//            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
//            SortedMap map = new TreeMap();
//            map.put("appId", channelPayConfig.getRsaPublicKey()); //应用ID
//            map.put("out_trade_no", payOrder.getPayOrderId());
//            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
//            _log.info(logPrefix + "******************sign:{}", sign);
//            map.put("sign", sign);
//
//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//
//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/payment-gateway/gateway/api/payTradeQuery.do?format=json", sendMsg);
//            _log.info("上游返回信息：" + res);
//            JSONObject resObj = JSONObject.parseObject(res);
//            String retCode = resObj.getString("code");
//            String retMsg = resObj.getString("msg");
//            JSONObject retData = (JSONObject) resObj.getJSONArray("data").get(0);
//            if (retCode.equals("200")) {
//                retObj.put("status", "2");
//            } else {
//                retObj.put("status", "1");
//            }
//            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retData.getString("status")) + "");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            return retObj;
//        } catch (Exception e) {
//            retObj.put("errDes", "操作失败!");
//            retObj.put("msg", "查询系统：请求上游发生异常！");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//            return retObj;
//        }
    }

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "161":
                return "未支付";
            case "162":
                return "支付成功";
            default:
                return "异常订单";
        }
    }
}
