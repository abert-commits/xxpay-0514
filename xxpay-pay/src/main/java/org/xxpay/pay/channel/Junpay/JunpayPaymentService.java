package org.xxpay.pay.channel.Junpay;

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

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class JunpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JunpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JUNZHIFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doJunpayPayReq(payOrder, "pddAliPayH5");//俊支付拼多多SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJunpayPayReq(payOrder, "aliPayH5");//俊咸鱼支付宝SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
                retObj = doJunpayPayReq(payOrder, "aliPaysdk");//俊支付拼多多SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJunpayPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【俊支支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject(16, true);
            map.put("version", "v1.0");//版本编号
            map.put("userId", channelPayConfig.getMchId());// 商家号

            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            map.put("type", channel); //支付产品ID
            map.put("requestNo", payOrder.getPayOrderId()); //商户订单号
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("callBackURL", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("redirectUrl", payConfig.getReturnUrl(getChannelName()));//同步回调地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("attachData", "perfect"); //附加参数
            map.put("sign", sign); //签名

            String sendMsg = map.toJSONString();
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retMsg = resObj.getString("message");
            if (resObj.getString("status").equals("1")) {
                String payJumpUrl = resObj.getString("payUrl");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    if (payJumpUrl.contains("<form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    }
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
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
        String logPrefix = "【俊支支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("userId", channelPayConfig.getMchId());// 商家号
            map.put("requestNo", payOrder.getPayOrderId()); //商户订单号
            map.put("orderNo", "orderNo"); //是否执行回调

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/query/pay", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("status");
            String retMsg = resObj.getString("message");
            if (retCode.equals("1")) {
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
