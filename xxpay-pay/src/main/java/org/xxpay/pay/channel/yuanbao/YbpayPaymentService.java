package org.xxpay.pay.channel.yuanbao;

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

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YbpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YbpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HANXIN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
                retObj = doYuanBaoPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doYuanBaoPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【元宝当面付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            String money = AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()));

            SortedMap map = new TreeMap();
            map.put("accountId", channelPayConfig.getMchId()); // 商户号
            map.put("outTradeNo", payOrder.getPayOrderId()); // 订单号
            map.put("subject", "支付" + money + "元"); // 订单内容
            map.put("amount", money); //支付金额
            map.put("callbackUrl", payConfig.getNotifyUrl(channelName)); // 支付结果后台回调URL
            map.put("successUrl", payConfig.getReturnUrl(channelName)); // 执行成功后跳转地址
            map.put("errorUrl", payConfig.getReturnUrl(channelName)); // 执行失败后跳转地址

            String signStr = "" + map.get("amount") + map.get("outTradeNo");
            String sign = Sign.get(signStr, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/alipay/checkpoint", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);

            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");

            if ("200".equals(retCode)) {
                String payJumpUrl = resObj.getString("data");
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
            } else {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
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
        String logPrefix = "【元宝当面付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        retObj.put("errDes", "操作失败!");
        retObj.put("msg", "查询系统：请求上游无查询通道！");
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        return retObj;
//        try {
//            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
//            SortedMap map = new TreeMap();
//            map.put("mchid", channelPayConfig.getMchId()); //应用ID
//            map.put("orderno", payOrder.getPayOrderId());
//            map.put("amount", String.valueOf(payOrder.getAmount()));
//
//            String signStr = map.get("orderno") + channelPayConfig.getmD5Key();
//            String sign = PayDigestUtil.md5(signStr, "UTF-8").toLowerCase();
//            _log.info(logPrefix + "******************sign:{}", sign);
//            map.put("sign", sign);
//
//            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//
//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/vip/PayOrderInfo", sendMsg);
//            _log.info("上游返回信息：" + res);
//            JSONObject resObj = JSONObject.parseObject(res);
//
//            String retCode = resObj.getString("orderno");
//            String retMsg = resObj.getString("OrderState");
//            if (!StringUtils.isBlank(retCode)) {
//                retObj.put("status", "2");
//            } else {
//                retObj.put("status", "1");
//            }
//            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
//            return retObj;
//        } catch (Exception e) {
//            retObj.put("errDes", "操作失败!");
//            retObj.put("msg", "查询系统：请求上游发生异常！");
//            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
//            return retObj;
//        }
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

    public static void main(String[] args) {
        String order_no = "P01202012202011093970000";
        String s_id = "16084531929421603067";
        String amount = "5";
        String notify_url = "http://xiaojack.natapp1.cc/notify/Dema/notify_res.htm";
        String md5 = amount + notify_url + order_no + s_id + "KIDyvufQSI6co4fCVka9ZtYpRT3z4abAR2dvkA01RLB6XFWVWcM9NXLcjTp0HxQd";
        String sign = PayDigestUtil.md5(md5, "UTF-8").toUpperCase();
        StringBuffer stringBuffer=new StringBuffer("http://103.85.225.26:9009/Order/Api/create?");
        stringBuffer.append("order_no=" + order_no).
                append("&s_id=" + s_id).
                append("&amount=" + amount).
                append("&notify_url=" + notify_url).
                append("&sign=" + sign);
        String url = stringBuffer.toString();
        String s = XXPayUtil.doGet(url);
        System.out.println(new Date());
        System.out.println(new Date()+":时间："+s);
    }
}
