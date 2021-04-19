package org.xxpay.pay.channel.dami;

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
public class DamiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DamiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAMI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doDaMiPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDaMiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【大咪闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String money = AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()));
            SortedMap map = new TreeMap();
            map.put("merchant_id", channelPayConfig.getMchId()); // 商户号
            map.put("orderid", payOrder.getPayOrderId()); // 订单号
            map.put("amount", money); //支付金额
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); // 支付结果后台回调URL
            map.put("pay_type", channelPayConfig.getChannelId()); // 支付方式

            String signStr = "merchant_id="+ channelPayConfig.getMchId() + "&orderid="+payOrder.getPayOrderId()+"&amount="+ money +"&notify_url="+payConfig.getNotifyUrl(getChannelName())+"&key="+channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            map.put("ns_request", "1"); // 支付方式

            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/index/pay/create_order", jsonObj.toJSONString());

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/Pay/create_order", sendMsg, "utf-8", "application/json");
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retMsg = resObj.getString("msg");
            if (!StringUtils.isBlank(retMsg)) {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            } else  {
                String payJumpUrl = resObj.getString("orderInfo");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
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
        String logPrefix = "【大咪闲鱼支付统一下单】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("orderid", payOrder.getPayOrderId()); //商户订单号

            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/index/api/orderQuery", jsonObj.toJSONString());
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");
            if (retCode.equals("1")) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retData.getString("status"))+ "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg+ "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

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
            case "0":
                return "未支付";
            case "1":
                return "支付成功";
            default:
                return "异常订单";
        }
    }
}
