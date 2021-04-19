package org.xxpay.pay.channel.yifeng;

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
public class YifengPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YifengPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        retObj = doHuaFeiPayReq(payOrder, "");
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【一风支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("productId", channelPayConfig.getChannelId()); //支付产品ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/create_order", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("retCode");
            String retMsg = resObj.getString("retMsg");
            String errCode = resObj.getString("errCode");
            if ("SUCCESS".equals(resultCode)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");
                // md5 加密
                if (resObj.containsKey("errCode")) {
                    _log.error("下单失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", res);
                    payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:" + res);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                String payJumpUrl = resObj.getJSONObject("payParams").getString("payUrl");
                String payMethod = PayConstant.PAY_METHOD_URL_JUMP;
                JSONObject payParams = new JSONObject();
                payParams.put("payMethod", payMethod);
                payParams.put("payJumpUrl", payJumpUrl);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {

                _log.info("一风支付返回失败信息：" + retMsg);
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:[" + retMsg + "]");
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
        String logPrefix = "【一风支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号

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
            case 4:
                return "已退款";
            default:
                return "交易失败";

        }
    }
}
