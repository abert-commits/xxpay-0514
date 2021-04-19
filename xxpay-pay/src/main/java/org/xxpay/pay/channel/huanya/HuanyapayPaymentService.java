package org.xxpay.pay.channel.huanya;

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
public class HuanyapayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HuanyapayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUANYA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuanYaPayReq(payOrder, "52");//ALISCAN 支付宝扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuanYaPayReq(payOrder, "62"); //ALIH5 支付宝 H5 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuanYaPayReq(payOrder, "72"); //ALIH5 支付宝 sdk 支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doHuanYaPayReq(payOrder, "51"); //WXSCAN 微信扫码支付
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHuanYaPayReq(payOrder, "61"); //微信 H5 支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuanYaPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【环亚支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            JSONObject map = new JSONObject();
            map.put("user_id", channelPayConfig.getMchId());// 用户ID
            map.put("trade_type", channel); //交易类型
            map.put("amount", String.valueOf(payOrder.getAmount())); // 金额 分
            map.put("order_id", payOrder.getPayOrderId());// 订单ID
            map.put("service_id", "trade.order");//接口名称
            map.put("version", "V001");//版本号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/qrpay/mvc/api", map.toJSONString());
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("0000".equals(resultCode)) {
              /*  // 第一步拼接字符串：
                SortedMap mapJM = new TreeMap();
                mapJM.put("code", resObj.getString("code"));//状态码
                mapJM.put("msg", resObj.getString("msg"));//返回说明
                mapJM.put("pay_url", resObj.getString("pay_url"));//支付链接
                mapJM.put("pay_img", resObj.getString("pay_img"));//支付二维码图片地址
                String md5 = PayDigestUtil.getSign(mapJM, channelPayConfig.getmD5Key());
                if (md5.equals(resObj.getString("sign"))) {*/
                String payJumpUrl = resObj.getString("pay_url");
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
                    if(payJumpUrl.contains("form"))
                    {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    }else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                /*} else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }*/
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
        String logPrefix = "【环亚支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("user_id", channelPayConfig.getMchId());// 用户ID
            map.put("version", String.valueOf(payOrder.getAmount())); // 金额 分
            map.put("order_id", payOrder.getPayOrderId());// 订单ID
            map.put("service_id", "trade.order.query");//接口名称
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            //Map 转成  JSONObject 字符串
            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/qrpay/mvc/api", jsonObj.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("code");
            String errMsg = resObj.getString("errMsg");

            if (code.equals("0000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("code", code);//商户编号
                map1.put("errMsg", errMsg);
                map1.put("msg", resObj.getString("msg"));
                map1.put("status", resObj.getString("status"));
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

            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(code)+ "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private  String GetStatusMsg(String code)
    {
        switch (code)
        {
            case  "0000": return  "支付成功";
            default: return  "支付中";
        }
    }
}
