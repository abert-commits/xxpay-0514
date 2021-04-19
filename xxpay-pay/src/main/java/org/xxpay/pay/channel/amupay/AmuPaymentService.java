package org.xxpay.pay.channel.amupay;

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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class AmuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(AmuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doPayReq(payOrder, "xydf");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【阿姆支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("version", "3.0");
            map.put("method", "Gt.online.interface");
            map.put("partner", channelPayConfig.getMchId());//商户号
            map.put("banktype", channel);//通道编码
            if (channelPayConfig.getRsapassWord() != null && StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                map.put("banktype", channelPayConfig.getRsapassWord());//通道编码
            }

            map.put("paymoney", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));//订单金额
            map.put("ordernumber", payOrder.getPayOrderId());//商户订单号
            map.put("callbackurl", payConfig.getNotifyUrl(channelName));
            map.put("hrefbackurl", "http://www.baidu.com");
            map.put("attach", "HXGoodsName");

            String signStr = MessageFormat.format("version={0}&method={1}&partner={2}&banktype={3}&paymoney={4}&ordernumber={5}&callbackurl={6}{7}",
                    map.get("version"), map.get("method"), map.get("partner"), map.get("banktype"), map.get("paymoney"), map.get("ordernumber"), map.get("callbackurl"), channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            map.put("notreturnpage", "true");

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/v1/getway", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            String retCode = resObj.getString("code");

            if (!retCode.equals("0")) {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("msg") + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject dataObj = resObj.getJSONObject("data");

            String payJumpUrl = dataObj.getString("payUrl");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                //SDK跳转
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (payOrder.getChannelId().contains("QR")) {
                //二维码
                payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
            } else {
                //表单跳转
                if (payJumpUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
            }

            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), dataObj.getString("tradeNo"));
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
        String logPrefix = "【阿姆支付订单查询】";
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "3.0");
            map.put("method", "Gt.online.query");//接口名称
            map.put("partner", channelPayConfig.getMchId());//商户号
            map.put("ordernumber", payOrder.getPayOrderId());//商户订单号
            map.put("sysnumber", payOrder.getChannelOrderNo());//上游訂單號

            String signStr = MessageFormat.format("version={0}&method={1}&partner={2}&ordernumber={3}&sysnumber={4}&key={5}",
                    map.get("version"), map.get("method"), map.get("partner"), map.get("ordernumber"), map.get("sysnumber"), channelPayConfig.getmD5Key());

            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/v1/getway", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            if (!retCode.equals("0")) {
                retObj.put("status", "1");
                retObj.put("msg", "查询失败,响应信息:" + resObj.getString("message"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }

            String payStatus = resObj.getJSONObject("data").getString("tradestate");
            switch (payStatus) {
                case "0":
                    retObj.put("status", "1");
                    retObj.put("msg", "支付中");
                    break;
                case "1":
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                    break;
                case "2":
                    retObj.put("status", "1");
                    retObj.put("msg", "支付失败");
                    break;
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
