package org.xxpay.pay.channel.yili;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.sandpay.sdk.AESTool;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

@Service
public class YilipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YilipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YILIPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY: // 支付宝 h5
                retObj = doYiliPayReq(payOrder, "17");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK: //支付宝 原生sdk
                retObj = doYiliPayReq(payOrder, "9");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR: //支付宝  扫码
                retObj = doYiliPayReq(payOrder, "4");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY: // 微信H5
                retObj = doYiliPayReq(payOrder, "15");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doYiliPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【一粒支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));


            Map jsonObject=new TreeMap();
            jsonObject.put("orgNo",channelPayConfig.getRsaPublicKey());  //接入机构编号
            jsonObject.put("merNo",channelPayConfig.getMchId());  //接入商户编号
            jsonObject.put("action","TradeOrderCreate"); //接口服务名称

            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("linkId",payOrder.getPayOrderId());//三方流水
            jsonObject1.put("payType", channel);                //支付类型
            jsonObject1.put("orderType","10");                //订单类型
            jsonObject1.put("goodsName","韩信支付");//商品名称
            jsonObject1.put("amount",String.valueOf(payOrder.getAmount()));//消费金额
            jsonObject1.put("notifyUrl",payConfig.getNotifyUrl(getChannelName()));//异步通知地址
            jsonObject.put("data",   AESTool.encrypt(jsonObject1.toJSONString(),channelPayConfig.getRsaprivateKey()));


            StringBuilder signBuffer = new StringBuilder();
            signBuffer.append(channelPayConfig.getRsaPublicKey());
            signBuffer.append(channelPayConfig.getMchId());
            signBuffer.append("TradeOrderCreate");
            signBuffer.append(AESTool.encrypt(jsonObject1.toJSONString(),channelPayConfig.getRsaprivateKey()));
            signBuffer.append(channelPayConfig.getmD5Key());

            String sign = PayDigestUtil.md5(signBuffer.toString(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            jsonObject.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(jsonObject).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/md5/action", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String respCode = resObj.getString("respCode");
            String respMsg = resObj.getString("respMsg");
            if ("200".equals(respCode)) {
                String data = AESTool.decrypt(resObj.getString("data"),channelPayConfig.getRsaprivateKey());
                JSONObject dataJson = JSONObject.parseObject(data);
                if ("000000".equals(dataJson.getString("code"))) {
                    String payJumpUrl = dataJson.getString("payUrl");
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
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), dataJson.getString("orderNo"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("下单失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败["+dataJson.getString("msg")+"]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
            } else {
                payInfo.put("errDes", "下单失败[" + respMsg + "]");
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
        String logPrefix = "【一粒支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            Map jsonObject=new TreeMap();
            jsonObject.put("orgNo",channelPayConfig.getRsaPublicKey());  //接入机构编号
            jsonObject.put("merNo",channelPayConfig.getMchId());  //接入商户编号
            jsonObject.put("action","OrderStatus"); //接口服务名称

            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("linkId",payOrder.getPayOrderId());//三方流水
            jsonObject.put("data",   AESTool.encrypt(jsonObject1.toJSONString(),channelPayConfig.getRsaprivateKey()));

            StringBuilder signBuffer = new StringBuilder();
            signBuffer.append(channelPayConfig.getRsaPublicKey());
            signBuffer.append(channelPayConfig.getMchId());
            signBuffer.append("OrderStatus");
            signBuffer.append(AESTool.encrypt(jsonObject1.toJSONString(),channelPayConfig.getRsaprivateKey()));
            signBuffer.append(channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signBuffer.toString(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            jsonObject.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(jsonObject).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/md5/action", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String respCode = resObj.getString("respCode");
            String respMsg = resObj.getString("respMsg");
            if ("200".equals(respCode)) {
                String data = AESTool.decrypt(resObj.getString("data"),channelPayConfig.getRsaprivateKey());
                JSONObject dataJson = JSONObject.parseObject(data);
                String msg=dataJson.getString("msg");
                if ("000000".equals(dataJson.getString("code"))) {
                    //支付成功
                   String orderStatus=dataJson.getString("orderStatus");
                    if(orderStatus.equals("20")){
                        retObj.put("status", "2");
                        retObj.put("msg",msg);
                    }else{
                        retObj.put("status", "1");
                        retObj.put("msg", "响应Code:" + dataJson.getString("code") + ",订单状态:" + GetStatusMsg(orderStatus)+ "");
                    }
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", msg);
                }

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private  String GetStatusMsg(String code)
    {
        switch (code)
        {
            case  "10": return  "初始化";
            case  "11": return  "等待支付";
            case  "12": return  "等待确认";
            case  "13": return  "支付中";
            case  "20": return  "支付成功";
            case  "21": return  "支付失败";
            case  "30": return  "订单审核";
            case  "99": return  "订单保留";
            default: return  "交易失败";
        }
    }
}
