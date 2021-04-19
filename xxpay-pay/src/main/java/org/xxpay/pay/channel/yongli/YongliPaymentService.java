package org.xxpay.pay.channel.yongli;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YongliPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YongliPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        retObj = doHuaFeiPayReq(payOrder, "");
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【永利支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());// 商家号
            map.put("pass_code", channelPayConfig.getChannelId()); //通道ID
            map.put("subject", "Shopping");
            map.put("body", "Shopping");
            map.put("out_trade_no", payOrder.getPayOrderId());
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("client_ip", "234.234.23.19");
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            map.put("timestamp", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
            String strSign = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(strSign + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/unifiedorder", JSON.toJSONString(map));
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("0".equals(resultCode)) {
                JSONObject dataObject = JSONObject.parseObject(resObj.getString("data"));
                String payJumpUrl = dataObject.getString("pay_url");
                JSONObject payParams = new JSONObject();
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                payParams.put("payJumpUrl", payJumpUrl);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), dataObject.getString("original_trade_no"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                _log.info("永利支付返回失败信息：" + retMsg);
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败:[" + retMsg + "]");
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
        String logPrefix = "【永利支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_id", channelPayConfig.getMchId());// 商家号
            map.put("out_trade_no", payOrder.getPayOrderId()); //商户订单号
            map.put("timestamp", "false"); //是否执行回调

            map.put("timestamp", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
            String strSign = XXPayUtil.mapToString(map);
            String sign = PayDigestUtil.md5(strSign + channelPayConfig.getmD5Key(), "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/query", JSON.toJSONString(map));
//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if (retCode.equals("0")) {
                JSONObject dataObj=resObj.getJSONObject("data");
                retObj.put("status", "1");
                if (dataObj.getInteger("status")==2)
                {
                    retObj.put("status", "2");
                }
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(dataObj.getInteger("status")) + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "上游订单查询失败=>" + retMsg);
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
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
                return "失败";
            case 1:
                return "待支付";
            case 2:
                return "支付成功";
            default:
                return "订单状态不明确";

        }
    }
}
