package org.xxpay.pay.channel.fuhao;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLEncoder;
import java.util.*;

@Service
public class FuhaoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(FuhaoPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DABAO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doDabaoRyPayReq(payOrder, "tb"); //大宝宝拼多多SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doDabaoRyPayReq(payOrder, "tb"); //大宝宝拼多多SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doDabaoRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【大宝宝支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            Map<String,String> map = new TreeMap();
            map.put("outTradeNo", payOrder.getPayOrderId()); //订单编号
            map.put("money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));  //支付金额
            map.put("appId", channelPayConfig.getMchId());// 商户号
            map.put("userAgent", "AlipayClient");// 订单类型
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("charset","utf-8");
            map.put("channelType", pay_code); //支付方式
            map.put("returnType", "app");// 商家号
            map.put("checkType", "md5");// 商家号


            String sign = PayDigestUtil.md5( channelPayConfig.getMchId()
                    +map.get("money")+map.get("outTradeNo")+
                    channelPayConfig.getmD5Key(),"utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pdd/friendPay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("message");
            String status = resObj.getString("status");
            if ("200".equals(resultCode) && status.equals("0")) {
                String payJumpUrl = resObj.getJSONObject("result").getString("url");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_sn"));
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
     * @param
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【大宝宝支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("outTradeNo", payOrder.getChannelOrderNo());//查询订单号
            map.put("appId", channelPayConfig.getMchId());//商户编号
            map.put("charset", "utf-8");//商户编号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/orderinfo", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");
            String status = resObj.getString("status");
            if (code.equals("10000") && status.equals("2")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("orderSn", resObj.getString("orderSn"));//商户编号
                map1.put("charset", resObj.getString("charset"));
                map1.put("outTradeNo", resObj.getString("outTradeNo"));
                map1.put("userAgent", resObj.getString("userAgent"));
                map1.put("appId", resObj.getString("appId"));
                map1.put("money", resObj.getString("money"));
                map1.put("addTime", resObj.getString("addTime"));
                map1.put("deliveryTime", resObj.getString("deliveryTime"));
                map1.put("endTime", resObj.getString("endTime"));
                map1.put("respMsg", resObj.getString("respMsg"));
                map1.put("respCode", resObj.getString("respCode"));
                map1.put("status", resObj.getString("status"));

                map1.put("remark", resObj.getString("remark"));
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            } else {
                if (resObj.getString("status").equals("2")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
            }
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    public static String getSign(Map<String, Object> map, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() + PayDigestUtil.getSortJson((JSONObject) entry.getValue()));
                } else {
                    list.add(entry.getKey() + entry.getValue());
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result = key + result + key;
        _log.info("Sign Before MD5:" + result);
        result = PayDigestUtil.md5(result, "UTF-8").toUpperCase();
        _log.info("Sign Result:" + result);
        return result;
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "订单生成";
            case "1":
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "业务处理完成";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
