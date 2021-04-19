package org.xxpay.pay.channel.yirun;

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
public class YirunPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YirunPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YIRUN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuaFeiPayReq(payOrder, "ZFB"); //pay.alipay.scan支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【亿润支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            map.put("appid", channelPayConfig.getMchId());// 商家号
            map.put("total_amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号
            map.put("version", "V1.0"); //币种
            map.put("callback_url", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("code", channel); //支付产品ID

            String sign = PayDigestUtil.getSignCustomizeKey(map, channelPayConfig.getmD5Key(),"appsecret");

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/pay?"+sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            if (!res.contains("msg")) {
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", res);
                    if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (res.contains("form")) {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                JSONObject resObj = JSONObject.parseObject(res);
                String msg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败[" + msg + "]");
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
        String logPrefix = "【亿润支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merid", channelPayConfig.getMchId());// 商家号
            map.put("order_no", payOrder.getPayOrderId()); //商户订单号

            String sign = PayDigestUtil.getSignCustomizeKey(map, channelPayConfig.getmD5Key(),"appsecret");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/queryOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String pay_status = resObj.getJSONObject("obj").getJSONObject("data").getString("pay_status");
            if (pay_status.equals("1")) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + pay_status + ",订单状态:成功");

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + pay_status + ",订单状态:未成功");

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

}
