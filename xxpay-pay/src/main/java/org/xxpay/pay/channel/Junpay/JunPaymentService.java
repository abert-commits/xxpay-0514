package org.xxpay.pay.channel.Junpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
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
public class JunPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JunPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJunPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJunPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【俊支付闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "v1.0");//版本编号
            map.put("type", channelPayConfig.getChannelId()); // 支付方式
            map.put("userId", channelPayConfig.getMchId()); // 商户号
            map.put("requestNo", payOrder.getPayOrderId()); // 订单号
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("callBackURL", payConfig.getNotifyUrl(getChannelName())); // 支付结果后台回调URL
            map.put("redirectUrl", payConfig.getReturnUrl(getChannelName())); // 支付结果后台回调URL

//            String signStr = XXPayUtil.mapToString(map);
//            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("attachData", "perfect"); //附加参数
            map.put("sign", sign); //签名

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/createOrder", sendMsg, "utf-8");
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retMsg = resObj.getString("message");
            if (resObj.getString("status").equals("1")) {
                String payJumpUrl = resObj.getString("payUrl");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("orderNo"));
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
        String logPrefix = "【俊支付闲鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("userId", channelPayConfig.getMchId());// 商家号
            map.put("requestNo", payOrder.getPayOrderId()); //商户订单号
            map.put("orderNo", payOrder.getChannelOrderNo());

//            String signStr = XXPayUtil.mapToString(map);
//            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = jsonObject.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/queryOrder", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/query/pay", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retStatus = resObj.getString("status");
            String retMsg = resObj.getString("message");
            if (StringUtils.isBlank(retMsg)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + retStatus + ",订单状态:" + GetStatusMsg(retStatus) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retStatus + ",订单状态:" + retMsg + "");
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
        switch (code) {
            case "1":
                return "预下单成功";
            case "2":
                return "预下单失败";
            case "3":
                return "交易成功";
            case "4":
                return "交易超时";
            case "5":
                return "交易失败";
            case "6":
                return "处理中";
            default:
                return "异常订单";
        }
    }
}
