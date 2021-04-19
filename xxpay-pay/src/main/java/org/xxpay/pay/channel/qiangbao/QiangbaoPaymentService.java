package org.xxpay.pay.channel.qiangbao;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QiangbaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QiangbaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        retObj = doPayReq(payOrder, ""); //pay.alipay.h5 支付宝 SDK
        return retObj;
    }

    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【强宝支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());// 商户ID
            map.put("orderid", payOrder.getPayOrderId());// 商户订单号
            map.put("money", String.valueOf(payOrder.getAmount()));// 订单金额 单位分
            map.put("type", channelPayConfig.getChannelId());// 支付方式
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName));// 商户接收后台返回储值结果的地址
            map.put("applytime", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));// 订单时间


            String strSign = XXPayUtil.mapToString(map) + channelPayConfig.getmD5Key();
            String sign = PayDigestUtil.md5(strSign, "utf-8");
            map.put("sign", sign.toLowerCase());// 签名数据
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            map.put("attach", "json");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String formHtml = XXPayUtil.buildRequestHtml(map, channelPayConfig.getReqUrl() + "/index.php/pay");

            String res = XXPayUtil.http(channelPayConfig.getReqUrl() + "/index.php/pay", map);
            JSONObject object = JSONObject.parseObject(res);
            String retCode = object.getString("status");
            String retMsg = object.getString("msg");
            if (!retCode.equals("1")) {
                payInfo.put("errDes", retMsg);
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:" + retMsg);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            String payJumpUrl = object.getString("payUrl");
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", payJumpUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (payJumpUrl.contains("<form") || payJumpUrl.contains("window.location")) {
                //表单跳转
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
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
        String logPrefix = "【强宝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            retObj.put("status", "1");
            retObj.put("msg", "上游没有查询接口！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
