package org.xxpay.pay.channel.hongye;

import com.alibaba.fastjson.JSONObject;
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
public class HongyePaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HongyePaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YUNKUOPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doYunkuoRyPayReq(payOrder, ""); //支付宝拼多多
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doYunkuoRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【鸿业支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("money", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 金额
            map.put("part_sn", payOrder.getPayOrderId());//订单号
            map.put("notify", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("id", channelPayConfig.getMchId());//商户号

            _log.info("鸿业下单验签串<<>>>+++"+channelPayConfig.getmD5Key() +"id"+channelPayConfig.getMchId()+"money"+map.get("money")+"notify"+payConfig.getNotifyUrl(channelName)+"part_sn"+payOrder.getPayOrderId()+ channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(channelPayConfig.getmD5Key() +"id"+channelPayConfig.getMchId()+"money"+map.get("money")+"notify"+payConfig.getNotifyUrl(channelName)+"part_sn"+payOrder.getPayOrderId()+ channelPayConfig.getmD5Key(), "UTF-8").toLowerCase();

            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            System.out.println(map.toString());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/order/index",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("1")) {
                String payJumpUrl = resObj.getJSONObject("data").getString("pay_str");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", URLDecoder.decode(payJumpUrl,"UTF-8"));
                if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else {
                    if (payJumpUrl.contains("form")) {
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
        String logPrefix = "【鸿业支付订单查询】";
      /*  JSONObject payInfo = new JSONObject();*/
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String res = (String) XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() +
                            "/api/Crontabpc/getOrderStatus?order_sn="+payOrder.getPayOrderId());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("1")) {

                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + resObj.getString("code") + ",订单状态: 已支付" );
            } else {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + resObj.getString("code") + ",订单状态: 未支付 " );
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
