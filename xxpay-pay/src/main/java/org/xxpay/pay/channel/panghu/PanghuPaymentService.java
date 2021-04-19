package org.xxpay.pay.channel.panghu;

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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

@Service
public class PanghuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(PanghuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MIDOU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doPayReq(payOrder, "alisdk"); //支付宝PDD-SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doPayReq(payOrder, "alipay"); //支付宝PDD-H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【胖虎支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("app_code", channelPayConfig.getMchId());//商户号
            map.put("nonce_str", DateUtil.getRevTime());//随机数
            map.put("out_trade_no", payOrder.getPayOrderId()); //订单号
            map.put("total_fee", String.valueOf(payOrder.getAmount())); //订单金额
            map.put("pay_type", pay_code);// 渠道code
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("remark", "shoping"); //备注
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/open/prepay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            JSONObject response = resObj.getJSONObject("response");
            String retCode = response.getString("code");
            if (!retCode.equals("10000")) {
                String sub_msg = response.getString("sub_msg");
                payInfo.put("errDes", "下单失败[" + sub_msg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }


            JSONObject payParams = new JSONObject();
            String payJumpUrl = response.getString("pay_params");
            payParams.put("payJumpUrl", payJumpUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (payJumpUrl.contains("form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }

            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
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
        String logPrefix = "【胖虎支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("app_code", channelPayConfig.getMchId());//商户号
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign",sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/open/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            JSONObject response = resObj.getJSONObject("response");
            String code = response.getString("code");
            if (!code.equals("10000"))
            {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + code + "上游查询失败，信息："+response.getString("sub_msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String status=response.getString("status");
            retObj.put("status", "1");
            if (status.equals("5"))
            {
                retObj.put("status", "2");
            }
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
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
            case "5":
                return "已支付";
            case "3":
                return "已取消";
            case "4":
                return "支付中";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
