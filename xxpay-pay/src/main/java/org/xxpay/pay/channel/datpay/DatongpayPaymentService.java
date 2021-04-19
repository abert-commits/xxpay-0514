package org.xxpay.pay.channel.datpay;

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

import java.net.URLDecoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class DatongpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DatongpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DATONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doDatongPayReq(payOrder, "8025"); //pay.alipay.h5 支付宝H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doDatongPayReq(payOrder, "8024"); //支付宝sdk
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDatongPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【大通支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }

            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("productId", channel); //支付产品ID
            if (channelPayConfig.getChannelId()!=null)
            {
                map.put("productId", channelPayConfig.getChannelId()); //支付产品ID
            }

            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("currency", "cny"); //币种
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("subject", "支付"); //商品主题
            map.put("body", "韩信支付"); //商品描述信息
            map.put("extra", ""); //附加参数
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

            if ("SUCCESS".equals(resultCode)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {

                    String channelName = payOrder.getChannelId().substring(payOrder.getChannelId().indexOf("_") + 1);
                    JSONObject payParams = new JSONObject();
                    if (channelName.contains("SDK")) {
                        String payJumpUrl = resObj.getJSONObject("payParams").getString("orderstr");
                        payParams.put("payJumpUrl", payJumpUrl);
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else {
                        String payJumpUrl = resObj.getJSONObject("payParams").getString("codeUrl");
                        payParams.put("payJumpUrl", payJumpUrl);
                        if (payJumpUrl.contains("<form>")) {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                        } else {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                        }
                    }
                    payInfo.put("payParams", payParams);

                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
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
        String logPrefix = "【大通支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("executeNotify", "false"); //是否执行回调

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
            default:
                return "交易失败";
        }
    }
}