package org.xxpay.pay.channel.hxpaofen;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class PaofenPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(PaofenPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.PAY_CHANNEL_HX_PAOFEN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doXianYuPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doXianYuPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【韩信跑分统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("merchantNo", channelPayConfig.getMchId());
            map.put("outOrderNo", payOrder.getPayOrderId());
            map.put("amount", payOrder.getAmount().toString()); //支付金额
            map.put("payType", channelPayConfig.getChannelId());
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            String mapString = "" + map.get("merchantNo") + map.get("outOrderNo") + map.get("amount") + map.get("payType") + map.get("notifyUrl");

            _log.info(logPrefix + "******************mapToString:{}", mapString);

            String sign = PayDigestUtil.md5(mapString + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名


            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/placeorder", sendMsg, "utf-8");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            String payJumpUrl = resObj.getString("info");
            if (resultCode.toUpperCase().equals("OK")) {
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
                Integer index=payJumpUrl.indexOf("=");
                String orderId=payJumpUrl.substring(index++);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), orderId);
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
        String logPrefix = "【韩信跑分订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchantNo", channelPayConfig.getMchId());
            map.put("outOrderNo", payOrder.getPayOrderId());

            String mapString = "" + map.get("merchantNo") + map.get("outOrderNo");
            _log.info(logPrefix + "******************mapToString:{}", mapString);
            String sign = PayDigestUtil.md5(mapString + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/queryorder", sendMsg, "utf-8");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            QueryInfo info = resObj.getObject("info", QueryInfo.class);

            if (resultCode.toUpperCase().equals("OK")) {
                if (info.getStatus().equals("0") || info.getStatus().equals("1") || info.getStatus().equals("2")
                        || info.getStatus().equals("3") || info.getStatus().equals("5")
                        || info.getStatus().equals("6") || info.getStatus().equals("7") || info.getStatus().equals("8")) {
                    retObj.put("status", 1);
                    retObj.put("msg", String.format("响应code:%s,订单状态:%s", info.getStatus(), GetStatusMsg(info.getStatus())));//"msg", "响应Code:" + resultCode + ",:"
                }else {
                    retObj.put("status", 2);
                    retObj.put("msg", String.format("响应code:%s,订单状态:%s", info.getStatus(), GetStatusMsg(info.getStatus())));
                }
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + resultCode + ",查询订单失败:" + retMsg);
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

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
//                订单生成("0", "订单生成"),
//                支付中("1", "支付中"),
//                支付成功("2", "支付成功"),
//                订单超时("3", "订单超时"),
//                订单完成("4", "订单完成"),
//                金额不一致("5", "金额不一致"),
//                账号不匹配("6", "账号不匹配"),
//                回调失败("7", "回调失败"),
//                订单失效("8", "订单失效");
        switch (code) {
            case "0":
                return "订单生成";
            case "1":
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "订单超时";
            case "4":
                return "订单完成";
            case "5":
                return "金额不一致";
            case "6":
                return "账号不匹配";
            case "7":
                return "回调失败";
            case "8":
                return "订单失效";
            default:
                return "异常订单";
        }
    }
}
