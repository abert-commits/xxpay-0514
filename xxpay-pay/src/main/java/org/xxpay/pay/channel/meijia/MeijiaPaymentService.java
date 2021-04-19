package org.xxpay.pay.channel.meijia;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.awen.StringUtil;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class MeijiaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(MeijiaPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        retObj = doPayReq(payOrder, ""); //pay.alipay.h5 支付宝 SDK
        return retObj;
    }

    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【美家支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));


            //获取token
            SortedMap map = new TreeMap();
            map.put("username", channelPayConfig.getMchId());// 商户名
            map.put("mobile", channelPayConfig.getRsapassWord());// 手机号
            map.put("email", channelPayConfig.getRsaPublicKey());// 邮箱
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");

            String jsonToken = XXPayUtil.doPostQueryCmd(
                    channelPayConfig.getReqUrl() + "/api/Token/getToken", sendMsg);
            _log.info(logPrefix + "******************jsonToken:{}", jsonToken);
            JSONObject objectToken = JSONObject.parseObject(jsonToken);
            if (objectToken.getInteger("code") != 1) {
                payInfo.put("errDes", objectToken.getString("msg"));
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:" + objectToken.getString("msg"));
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }


            map = new TreeMap();
            map.put("token", objectToken.getString("token"));
            map.put("api_order_sn", payOrder.getPayOrderId());// 商户订单号
            map.put("hope_total", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 订单金额 单位分
            map.put("pay_type", channelPayConfig.getChannelId());// 支付方式
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notify_url", payConfig.getNotifyUrl(channelName)+"?token="+objectToken.getString("token"));// 商户接收后台返回储值结果的地址


            sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/Order/createOrder", sendMsg);
            JSONObject object = JSONObject.parseObject(res);
            String retCode = object.getString("code");
            String retMsg = object.getString("msg");
            if (StringUtil.isEmpty(retCode)||!retCode.equals("1")) {
                payInfo.put("errDes", retMsg);
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:" + retMsg);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payJumpUrl = object.getJSONObject("data").getString("payUrl");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (payJumpUrl.contains("<form") || payJumpUrl.contains("window.location")) {
                //表单跳转
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), objectToken.getString("token"));
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
        String logPrefix = "【美家支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            //获取token
            SortedMap map = new TreeMap();
            map.put("username", channelPayConfig.getMchId());// 商户名
            map.put("mobile", channelPayConfig.getRsapassWord());// 手机号
            map.put("email", channelPayConfig.getRsaPublicKey());// 邮箱
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");

            String jsonToken = XXPayUtil.doPostQueryCmd(
                    channelPayConfig.getReqUrl() + "/api/Token/getToken", sendMsg);
            JSONObject objectToken = JSONObject.parseObject(jsonToken);
            if (objectToken.getInteger("code") != 1) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败:" + objectToken.getString("msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;

            }
            map = new TreeMap();
            map.put("token", objectToken.getString("token"));//
            map.put("api_order_sn", payOrder.getPayOrderId());//

            sendMsg = XXPayUtil.mapToString(map).replace(">", "");

            String jsonpayStatus = XXPayUtil.doPostQueryCmd(
                    channelPayConfig.getReqUrl() + "/api/CheckOrder/payStatus", sendMsg);
            JSONObject objectjsonpayStatus = JSONObject.parseObject(jsonpayStatus);
            String s = "";
            if (objectjsonpayStatus.getJSONObject("data").get("order_status").equals("failed")) {
                s = "失败";
            } else {
                s = "成功";
            }
            retObj.put("status", "1");
            retObj.put("msg", s);
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
