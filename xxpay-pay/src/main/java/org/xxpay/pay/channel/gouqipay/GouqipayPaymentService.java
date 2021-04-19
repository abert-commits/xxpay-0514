package org.xxpay.pay.channel.gouqipay;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class GouqipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(GouqipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_GOUIQI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuaFeiPayReq(payOrder, ""); //pay.alipay.h5 支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【枸杞支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("productId", channelPayConfig.getChannelId()); //支付产品ID
//            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("currency", "cny"); //币种
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("subject", "支付"); //商品主题
            map.put("body", "韩信支付"); //商品描述信息
//            map.put("extra", ""); //附加参数
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String msg = resObj.getString("msg");

            if ("1".equals(resultCode)) {
                String payJumpUrl = URLDecoder.decode(resObj.getJSONObject("data").getString("pay_url"),"UTF-8");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);

                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (res.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("order_no"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("order_no"), result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error(msg, payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败，" + msg);
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
        String logPrefix = "【枸杞支付订单查询】";
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
