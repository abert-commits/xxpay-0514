package org.xxpay.pay.channel.yibaopay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YibaopayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YibaopayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XXO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //微信H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doXXOPayReq(payOrder, "UweChat");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doXXOPayReq(payOrder, "AliPayH5");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doXXOPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【易宝支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }
            SortedMap map = new TreeMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("mch_id", channelPayConfig.getMchId());//商户编号
            if (!StringUtils.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            map.put("trade_type", channel);
            map.put("money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//金额，单位元
            map.put("out_order_no", payOrder.getPayOrderId());//商户订单号
            map.put("notify_url", payConfig.getNotifyUrl(channelName));//异步回调地址
            map.put("back_url", payConfig.getReturnUrl(channelName));//同步回调地址
            map.put("attach", "HanXinShoping");//商品描述
            map.put("mch_create_ip", "127.0.0.1");//客户端ip
            map.put("username", "HanXin");
            map.put("userid", String.valueOf(System.currentTimeMillis()));
            map.put("body", "WLGW");

            String strSign = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(strSign + channelPayConfig.getmD5Key(), "UTF-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/index", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String message = resObj.getString("message");
            if ("0".equals(code)) {
                JSONObject payParams = new JSONObject();
                if (channelPayConfig.getRsaPublicKey().equals("SDK")) {
                    String payJumpUrl = message;
                    payParams.put("payJumpUrl", payJumpUrl);
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else {
                    String payJumpUrl = message;
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payJumpUrl.contains("form")) {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("orderNo"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败,失败信息：" + message);
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
        String logPrefix = "【易宝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());  //商户号，由支付平台分配
            map.put("out_order_no", payOrder.getPayOrderId()); //商户系统内部的订单号,32个字符内、可包含字母


            String signStr = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(signStr + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (Integer.parseInt(resObj.getString("code")) != 0) {
                retObj.put("status", "1");
                retObj.put("msg", "上游查询失败");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String return_code = resObj.getJSONObject("message").getString("state");
            if ("1".equals(return_code) || "2".equals(return_code)) {
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
