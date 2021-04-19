package org.xxpay.pay.channel.huatongfu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class HuatongfupayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HuatongfupayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUATONGFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "alipay_qrcode"); //pay.alipay.h5 支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【华通付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("account_id", channelPayConfig.getMchId());// 商家号
            map.put("thoroughfare", channel); //目前通 道
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", f1); //支付金额 元
            map.put("callback_url", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("success_url", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            map.put("error_url", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            map.put("nonce_str", RandomStringUtils.randomAlphanumeric(31));//随机数
            map.put("content_type", "json_new");//随机数
            map.put("robin", "1"); //支付产品ID
           // String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());


            //第一步：将金额转两位小数，即1.00，同时拼接上外部订单号，得到的结果为： MD5(1.0020191298678976567)	=	5fe80d9f797f3b5ffa543d7a6802a16e
            String md51=PayDigestUtil.md5(f1+payOrder.getPayOrderId(),"UTF-8");

            //第二步：将得到的字符串拼接上小写的MD5秘钥（商户后台获取）即： MD5(d93e4522687525438068cff627333e6d5fe80d9f797f3b5ffa543d7a6802a16e)
            String md52=PayDigestUtil.md5(channelPayConfig.getmD5Key().toLowerCase()+md51,"UTF-8");
            _log.info(logPrefix + "******************sign:{}", md52);

            map.put("sign", md52); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/Gateway/create", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);

            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("200".equals(resultCode)) {
                String payJumpUrl = resObj.getJSONObject("data").getString("jump_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (channel.equals("alipay_qrcode")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("id"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                payInfo.put("errDes", "下单失败[验签失败]");
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
        String logPrefix = "【华通付支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("acc_id", channelPayConfig.getMchId());// 商家号
            map.put("out_trade_no", payOrder.getPayOrderId()); //外部订单号
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", f1); //订单金额
            map.put("nonce_str", RandomStringUtils.randomAlphanumeric(31));//随机数
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);

            //第一步：将金额转两位小数，即1.00，同时拼接上外部订单号，得到的结果为： MD5(1.0020191298678976567)	=	5fe80d9f797f3b5ffa543d7a6802a16e
            String md51=PayDigestUtil.md5(f1+payOrder.getPayOrderId(),"UTF-8");

            //第二步：将得到的字符串拼接上小写的MD5秘钥（商户后台获取）即： MD5(d93e4522687525438068cff627333e6d5fe80d9f797f3b5ffa543d7a6802a16e)
            String md52=PayDigestUtil.md5(channelPayConfig.getmD5Key().toLowerCase()+md51,"UTF-8");
            _log.info(logPrefix + "******************sign:{}", md52);

            map.put("sign", md52); //签名


            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/gateway/order-query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("200")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retCode) + "");
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
            case "200":
                return "成功 ";
            case "400":
                return "订单正在进行中 ";
            default:
                return "交易失败";
        }
    }
}
