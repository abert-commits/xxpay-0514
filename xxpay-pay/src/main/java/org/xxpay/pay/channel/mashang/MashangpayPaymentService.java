package org.xxpay.pay.channel.mashang;

import com.alibaba.fastjson.JSONObject;
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
public class MashangpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(MashangpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MASHANG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doMaShangPayReq(payOrder, "1", "1");//ALISCAN 支付宝扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMaShangPayReq(payOrder, "1", "4"); //ALIH5 支付宝 H5 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doMaShangPayReq(payOrder, "1", "3"); //ALIH5 支付宝 sdk 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doMaShangPayReq(payOrder, "2", "1"); //WXSCAN 微信扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMaShangPayReq(payOrder, "2", "4"); //微信 H5 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY_SDK:
                retObj = doMaShangPayReq(payOrder, "2", "3"); //微信 SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doMaShangPayReq(PayOrder payOrder, String payType, String interFaceType) {
        String logPrefix = "【马上支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("version", "1.5");
            map.put("merchno", channelPayConfig.getMchId());// 用户ID
            map.put("goodsName", "大 好"); //商品描述
            map.put("traceno", payOrder.getPayOrderId());// 订单ID
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", amount); // 金额 元
            map.put("payType", payType);        //支付类型
            map.put("interfacetype", interFaceType);  //接口类型
            map.put("settleType", "1");   //结算周期
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //异步通知地址
            map.put("callbackUrl", payConfig.getReturnUrl(getChannelName())); //同步返回地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/b2c/payment/order", map.toJSONString());
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("respCode");
            String retMsg = resObj.getString("message");
            if ("1".equals(resultCode)) {
                // 第一步拼接字符串：
                SortedMap mapJM = new TreeMap();
                mapJM.put("barCode", resObj.getString("barCode"));//支付码信息
                mapJM.put("merchno", resObj.getString("merchno"));//商户号
                mapJM.put("traceno", resObj.getString("traceno"));//商户订单号
                mapJM.put("refno", resObj.getString("refno"));//上游渠道订单号
                mapJM.put("respCode", resObj.getString("respCode"));//返回状态码
                mapJM.put("message", resObj.getString("message"));//返回信息

                String md5 = PayDigestUtil.getSign(mapJM, channelPayConfig.getmD5Key());
                if (md5.equals(resObj.getString("sign"))) {
                    String payJumpUrl = resObj.getString("barCode");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (interFaceType.equals("1")) {
                        //二维码
                        payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                    } else if (interFaceType.equals("3")) {
                        //SDK跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else {
                        //表单跳转
                        if (payJumpUrl.contains("form")) {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                        } else {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                        }
                    }

                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("refno"));
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
        String logPrefix = "【马上支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("version", "1.5");// 版本
            map.put("merchno", channelPayConfig.getMchId());// 商户号
            map.put("traceno", payOrder.getPayOrderId());// 交易流水号
            map.put("queryType", "1");//查询类型
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());

            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/b2c/payment/query/orders", map.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");
            String errMsg = resObj.getString("message");
            if (code.equals("1")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("respCode", code);//商户编号
                map1.put("message", errMsg);
                map1.put("payStatus", resObj.getString("payStatus"));
                map1.put("traceno", resObj.getString("traceno"));
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                } else {
                    //支付成功
                    retObj.put("status", "2");
                }
            } else {
                retObj.put("status", "1");
            }

            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg( resObj.getString("payStatus")) + "");
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
            case "0":
                return "待支付";
            case "1":
                return "未支付";
            case "2":
                return "已支付";
            default:
                return "支付失败";
        }
    }
}
