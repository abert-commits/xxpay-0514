package org.xxpay.pay.channel.xiaodingdang;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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
public class XiaodingdangPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XiaodingdangPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YUANZHIFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuaFeiPayReq(payOrder, "qrcodeReceipt");// 支付宝扫码
                break;

            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "qrcodeReceipt"); //alipayh5 支付宝 H5
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【小叮当统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            map.put("version", "v1.0");// 版本号
            map.put("serviceType", channel); //业务编码
            map.put("merNo", channelPayConfig.getMchId()); //商户号
            map.put("requestNo", payOrder.getPayOrderId()); //流水号
            map.put("signType", "md5"); //签名类型

            map.put("merOrderNo", payOrder.getPayOrderId()); //商户号
            map.put("payType",channelPayConfig.getChannelId()); //
            map.put("tradeAmt", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("notifyUrl",payConfig.getNotifyUrl(channelName));// 回调地址


            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key()).toLowerCase();

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("signature", sign); //签名
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}",sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/Pay/backTransReq.ashx", JSONObject.toJSONString(map));
            _log.info(logPrefix + "******************上游返回数据:{}", res);


            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("respCode");
            String retMsg = resObj.getString("respDesc");
            if ("P000".equals(resultCode)) {
                    String payJumpUrl = resObj.getString("payUrl");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payJumpUrl.contains("<form")) {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(),"");
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
        String logPrefix = "【小叮当订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("version", "v1.0");// 版本号
            map.put("serviceType", "queryTradeStatus"); //业务编码
            map.put("merNo", payOrder.getPayOrderId()); //商户号
            map.put("requestNo", payOrder.getPayOrderId()); //流水号
            map.put("signType", "md5"); //签名类型

            map.put("merOrderNo", payOrder.getPayOrderId()); //商户订单号
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("signature", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay/backTransReq.ashx", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("msg")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getJSONObject("data").getInteger("status")) + "");
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
            case 2:
                return "未支付";
            case 3:
                return "订单超时";
            case 4:
                return "支付完成";
            default:
                return "交易失败";
        }
    }
}
