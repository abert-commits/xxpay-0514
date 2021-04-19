package org.xxpay.pay.channel.haixing;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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
public class HaixingPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HaixingPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HAIXING;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuaFeiPayReq(payOrder, "alipaygm_auto");// 支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【海鑫支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }

            map.put("account_id", channelPayConfig.getMchId());// 商家号
            map.put("content_type", "text");// 请求过程中返回的网页类型，text (扫码支付 )或 json（获取二维码）
            map.put("thoroughfare",channel);// 	初始化支付通道
            map.put("atype", "1");// 	支付类型
            map.put("out_trade_no",payOrder.getPayOrderId());// 订单信息
            map.put("robin", "2");// 	轮训
            map.put("keyId", "785D239777C4DE7739");// 设备KEY

            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));// 支付金额，在发起时用户填写的支付金额
            map.put("callback_url",payConfig.getNotifyUrl(getChannelName()));// 异步通知地址
            map.put("success_url",payConfig.getReturnUrl(getChannelName()));// 支付成功后网页自动跳转地址，
            map.put("error_url", payConfig.getReturnUrl(getChannelName()));// 支付失败时

            String sign = HaixingSign.sign(AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())),payOrder.getPayOrderId(),channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("sign",sign );// 签名算法，在支付时进行签名算法，详见上方算法

           // String sendMsg = XXPayUtil.mapToString(map).replace(">", "");

            String res = XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/gateway/index/checkpoint.do");
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", res);
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payInfo.put("payParams", payParams);
            rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);

            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

           /* JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");

            if ("200".equals(resultCode)) {
                    String payJumpUrl = resObj.getJSONObject("data").getString("qrcode");
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
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("order_id"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

            } else {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }*/
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
        String logPrefix = "【海鑫支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("executeNotify", "false"); //是否执行回调

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/query_order", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("retCode");
            String retMsg = resObj.getString("retMsg");
            if (retCode.equals("SUCCESS")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getInteger("status")) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(int code) {
        switch (code) {
            case 0:
                return "订单生成";
            case 1:
                return "支付中";
            case 2:
                return "支付成功";
            case 3:
                return "业务处理完成";
            default:
                return "交易失败";
        }
    }
}
