package org.xxpay.pay.channel.lintian;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.text.MessageFormat;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class LintianPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LintianPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SHUNAN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "Abankcard");//支付宝H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHuaFeiPayReq(payOrder, "wechat"); //微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【麟天支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap();
            map.put("merchantNum", channelPayConfig.getMchId());// 商家号
            map.put("orderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            String channelName =payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            if (channelPayConfig.getRsapassWord() != null && StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            map.put("payType", channel);
            String signStr = MessageFormat.format("{0}{1}{2}{3}{4}",
                    channelPayConfig.getMchId(), payOrder.getPayOrderId(), map.get("amount"), map.get("notifyUrl"), channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/startOrder", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String msg = resObj.getString("msg");

            if ("200".equals(code)) {
                String payJumpUrl = resObj.getJSONObject("data").getString("payUrl");
                String out_orderNo = resObj.getJSONObject("data").getString("id");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payJumpUrl.contains("<form>")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), out_orderNo);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("order_no"), result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
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
        String logPrefix = "【麟天支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap();
            map.put("merchantNum", channelPayConfig.getMchId());// 商家号
            map.put("merchantOrderNo", payOrder.getPayOrderId()); //商户订单号
            String sign = PayDigestUtil.md5(map.get("merchantNum") + map.get("merchantOrderNo") + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/api/getOrderInfo?" + sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String msg = resObj.getString("msg");
            if (!code.equals("200")) {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + code + ",msg:" + msg + "");
            }else {
                if (resObj.getJSONObject("data").getString("orderState").equals("4"))
                {
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                }else {
                    retObj.put("status", "1");
                    retObj.put("msg","未支付");
                }
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
