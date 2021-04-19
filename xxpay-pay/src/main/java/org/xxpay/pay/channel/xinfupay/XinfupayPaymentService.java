package org.xxpay.pay.channel.xinfupay;

import com.alibaba.fastjson.JSONObject;
import junit.framework.Test;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

@Service
public class XinfupayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XinfupayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XINFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doXinfuRyPayReq(payOrder, "qrcode2"); //支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doXinfuRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【新富支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            if (StringUtils.isNotBlank(channelPayConfig.getRsaPublicKey())) {
                pay_code = channelPayConfig.getRsaPublicKey();
            }
            SortedMap<String, String> map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("accessKey", channelPayConfig.getMchId());//支付私钥
            map.put("quartetProductPayType", pay_code);//支付方式
            map.put("outTradeNo", payOrder.getPayOrderId());// 商户订单号
            map.put("money", String.valueOf(amount));// 金额
            map.put("body", "nihao");//body
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("redirectUrl", payConfig.getReturnUrl(channelName));// 同步跳转地址
            map.put("timestamp", String.valueOf(System.currentTimeMillis())); //请求时间
            System.out.println("新富下单眼前串" + map);
            String signs = HmachshaUtil.createSign(channelPayConfig.getmD5Key(), map);

            _log.info(logPrefix + "******************sign:{}", signs);
            map.put("signature", signs);
            map.put("act", "preOrder");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());
            System.out.println("新富请求url:" + channelPayConfig.getReqUrl() + "/PayServlet?" + sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/PayServlet", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("success").equals("ok")) {
                String data = resObj.getString("data");
                JSONObject jsonObject = JSONObject.parseObject(data);
                String payXinfuUrl = jsonObject.getString("payUrl");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payXinfuUrl);
                //表单跳转
                if (payXinfuUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + res + "]");
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
        String logPrefix = "【新富支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("outTradeNo", payOrder.getPayOrderId());// 商户订单号
            map.put("accessKey", channelPayConfig.getMchId()); // 商户号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
            String signs = HmachshaUtil.createSign(channelPayConfig.getmD5Key(), map);

            _log.info(logPrefix + "******************sign:{}", signs);
            map.put("signature", signs);
            map.put("act", "queryOrderByOutTradeNo");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/PayServlet", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("success");
            if (code.equals("ok")) {
                if (resObj.getString("tradeState").equals("3")) {
                    retObj.put("tradeState", "2");
                } else {
                    retObj.put("tradeState", "1");
                }
            }
            String status = resObj.getString("tradeState");
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
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
            case "1":
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "处理完成";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
