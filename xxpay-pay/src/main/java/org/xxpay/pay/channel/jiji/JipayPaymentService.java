package org.xxpay.pay.channel.jiji;

import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.fastjson.JSONArray;
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

import java.net.URLDecoder;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class JipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJJPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJJPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【JJ闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId()); // 商户号
            map.put("pay_type", channelPayConfig.getChannelId()); // 支付方式
            map.put("amount", AmountUtil.convertCent2Dollar(payOrder.getAmount().toString())); //支付金额
            map.put("callback_url", payConfig.getNotifyUrl(channelName)); // 支付结果后台回调URL
            map.put("success_url", payConfig.getReturnUrl(channelName)); // 支付结果后台回调URL
            map.put("error_url", payConfig.getReturnUrl(channelName)); // 支付结果后台回调URL
            map.put("out_uid", String.valueOf((new Date()).getTime() / 1000));
            map.put("out_trade_no", payOrder.getPayOrderId()); // 订单号
            map.put("version", "v2.0");//版本编号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/unifiedorder?format=json", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);

            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONObject("data");
            if ("200".equals(retCode)) {
                String payJumpUrl = URLDecoder.decode(retData.getString("sdk_url"),"UTF-8") ;
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), retData.getString("order_no"));
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
        String logPrefix = "【JJ闲鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId());// 商家号
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/getorder", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            JSONObject retData = resObj.getJSONArray("data").getJSONObject(0);
            if ("200".equals(retCode)) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retData.getString("status")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
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

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "1":
                return "获取订单成功";
            case "2":
                return "未支付";
            case "3":
                return "订单超时";
            case "4":
                return "支付完成";
            default:
                return "异常订单";
        }
    }
}
