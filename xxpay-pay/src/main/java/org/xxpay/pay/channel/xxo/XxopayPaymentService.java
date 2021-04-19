package org.xxpay.pay.channel.xxo;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
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
public class XxopayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XxopayPaymentService.class);

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
                retObj = doXXOPayReq(payOrder, "weix");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doXXOPayReq(payOrder, "zfbh5");
                break;
            //支付宝扫码
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doXXOPayReq(payOrder, "zhifubsm");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XJ:
                retObj = doXXOPayReq(payOrder, "hbh5");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDD:
                retObj = doXXOPayReq(payOrder, "PDDh5");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XY:
                retObj = doXXOPayReq(payOrder, "daifuh5");
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doXXOPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【XXO付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }
            SortedMap map = new TreeMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("merchant", channelPayConfig.getMchId());//商户编号
            map.put("amount", String.valueOf(payOrder.getAmount()));//金额，单位为分

            if (!StringUtils.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            map.put("pay_type", channel);//支付产品类型 (如：zfbh5)
            map.put("order_no", payOrder.getPayOrderId());//商户订单号
            map.put("order_time", String.valueOf(System.currentTimeMillis()));//下单时间，Unix时间戳秒
            map.put("subject", "支付");//商品描述
            map.put("notify_url", payConfig.getNotifyUrl(channelName));//异步回调地址
            map.put("return_url", payConfig.getReturnUrl(channelName));//同步回调地址
            String sign = PayDigestUtil.getSignSha1(map, channelPayConfig.getmD5Key());
            map.put("client_ip", "127.0.0.1");//客户端ip


            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/addOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            Boolean Success = resObj.getBoolean("success");

            if ("0000".equals(code) && Success) {
                JSONObject result1 = resObj.getJSONObject("result");
                String signs = result1.getString("sign");
                result1.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSignSha1(result1, channelPayConfig.getmD5Key()).toUpperCase();
                if (md5.equals(signs)) {
                    JSONObject payParams = new JSONObject();

                    if (channelPayConfig.getRsapassWord().contains("SDK")) {
                        String payJumpUrl = result1.getString("appUrl");
                        payParams.put("payJumpUrl", payJumpUrl);
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else {
                        String payJumpUrl = result1.getString("qrUrl");
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
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
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


}
