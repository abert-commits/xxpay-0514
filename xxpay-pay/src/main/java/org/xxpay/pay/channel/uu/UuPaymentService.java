package org.xxpay.pay.channel.uu;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(UuPaymentService.class);

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
            //支付宝wap（H5）
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "1A");
                break;
            //微信wap（H5）
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "1D");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【UU支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            map.put("coopId", channelPayConfig.getMchId());//商户编号
            map.put("outOrderNo", payOrder.getPayOrderId());//商户订单号
            map.put("subject", "HX商品购买");//订单日期
            map.put("money", payOrder.getAmount().toString());// 订单金额 单位 分
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));
            map.put("pathType", channel);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String sign = PayDigestUtil.md5(sendMsg + channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/create", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            if (retCode.equals("0")) {
                String payJumpUrl = resObj.getString("payurl");
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
                    if (payJumpUrl.contains("form"))
                    {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    }else{
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("msg") + "]");
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
        String logPrefix = "【UU支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj=new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("coopId", channelPayConfig.getMchId());//商户编号
            map.put("outOrderNo", payOrder.getPayOrderId());//上游訂單號
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String sign = PayDigestUtil.md5(sendMsg + channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            JSONObject retObjTo = resObj.getJSONObject("ret");
            if (retObjTo==null)
            {
                retObj.put("status", "1");
                retObj.put("msg", "上游返回信息异常，返回信息："+resObj.toJSONString());
            }

            //金额校验
            BigDecimal money= new BigDecimal(retObjTo.getString("money")).multiply(new BigDecimal(100));
            if (money.intValue()!=payOrder.getAmount().intValue()) {
                retObj.put("status", "1");//支付成功
                retObj.put("msg","上游订单金额与本地订单金额不符合");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return  retObj;
            }

            if (retObjTo.getString("settleStatus").equals("1"))
            {
                retObj.put("status", "2");//支付成功
                retObj.put("msg", "上游订单状态："+GetStatus(retObjTo.getString("settleStatus")));
            }else {
                retObj.put("status", "1");
                retObj.put("msg", "上游订单状态："+GetStatus(retObjTo.getString("settleStatus"))+",失败信息："+retObjTo.getString("tftNotifyMsg"));
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }


    private  String GetStatus(String status)
    {
        switch (status)
        {
            case  "0": return  "支付失败";
            case  "1": return  "支付成功";
            case  "3": return  "处理中";
            default: return  "未知状态";
        }
    }
}
