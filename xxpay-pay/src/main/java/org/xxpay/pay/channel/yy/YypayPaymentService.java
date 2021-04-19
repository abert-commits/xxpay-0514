package org.xxpay.pay.channel.yy;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YypayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YypayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DOUDOU;
    }

    private static String encodingCharset = "UTF-8";
    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doDouDouRyPayReq(payOrder, ""); //YY话费H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDouDouRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【YY支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("merch_id", channelPayConfig.getMchId());//商户号
            map.put("order_id", payOrder.getPayOrderId());//订单号
            map.put("amount", String.valueOf(amount));// 金额
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("return_url", payConfig.getNotifyUrl(channelName)); //异步通知地址

            String sign = PayDigestUtil.getSign(map,channelPayConfig.getmD5Key());
            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map);


            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/create_order/v3",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            String msg=resObj.getString("msg");
            if (resObj.getString("state").equals("ok")) {
                String pay_url= resObj.getString("pay_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", pay_url);
                if (payOrder.getChannelId().contains("SDK")) {
                    if (pay_url.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    }
                } else {
                    if (pay_url.contains("<form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【YY支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merch_id", channelPayConfig.getMchId());//商户号
            map.put("order_id", payOrder.getPayOrderId());
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);


            String sendMsg =JSONObject.toJSONString(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/search_order",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String state = resObj.getString("state");
            String status = resObj.getString("status");

            if (state.equals("ok")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + status + ",订单状态:" + GetStatusMsg(status) + "");
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
            case "CREATE":
                return "创建";
            case "CLAIM":
                return "待付款";
            case "CONFIRM":
                return "已确认";
            case "SUCCESS":
                return "成功";
            case "FAIL":
                return "失败";
            case "CANCEL":
                return "取消";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
