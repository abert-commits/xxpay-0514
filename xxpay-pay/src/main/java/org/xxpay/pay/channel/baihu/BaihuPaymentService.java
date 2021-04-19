package org.xxpay.pay.channel.baihu;

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

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class BaihuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(BaihuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YUANZHIFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHuaFeiPayReq(payOrder, "2");//wechath5微信 H5
                break;

            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "1"); //alipayh5 支付宝 H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doHuaFeiPayReq(payOrder, "3");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {

        String logPrefix = "【白虎支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            if (!StringUtil.isBlank(channelPayConfig.getChannelId())) {
                channel = channelPayConfig.getChannelId();
            }
            map.put("merchant_id", channelPayConfig.getMchId());// 商家号
            map.put("pay_type", channel); //支付产品ID
            map.put("payment_money", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("merchant_order_id", payOrder.getPayOrderId()); //商户订单号
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("pay_notify_url",payConfig.getNotifyUrl(channelName));// 回调地址
            map.put("pay_return_url",payConfig.getReturnUrl(channelName));// 支付成功后网页自动跳转地址
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String payUrl = XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/api/pay");
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(),null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            JSONObject payParams = new JSONObject();
            payParams.put("payMethod",PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl",payUrl);
            payInfo.put("payParams",payParams);
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
        String logPrefix = "【白虎支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchant_id", channelPayConfig.getMchId());// 商家号
            map.put("merchant_order_id", payOrder.getPayOrderId()); //商户订单号
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/searchMerchantOrder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("message");
            if (retCode.equals("200")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" +retMsg + "");
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
            case "CODE_SUCCESS":
                return "已支付";
            case "CODE_FAILURE":
                return "未支付";
            default:
                return "交易失败";
        }
    }
}
