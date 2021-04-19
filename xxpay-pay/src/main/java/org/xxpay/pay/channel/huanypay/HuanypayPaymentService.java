package org.xxpay.pay.channel.huanypay;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
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
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class HuanypayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(HuanypayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUANY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJuheRyPayReq(payOrder, "904"); //支付宝SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doJuheRyPayReq(payOrder, "901"); //支付宝拼多多SDK
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【环亚支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_amount", String.valueOf(amount));// 金额
            map.put("pay_memberid", channelPayConfig.getMchId());//商户号
            map.put("pay_orderid", payOrder.getPayOrderId());//订单号
            map.put("pay_applydate", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); //请求时间
            map.put("pay_bankcode", pay_code); //银行编码
            map.put("pay_notifyurl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("pay_callbackurl", payConfig.getReturnUrl(channelName)); //支付完成返回地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            map.put("pay_md5sign", sign);// md5签名
            map.put("pay_productname", "你好");// 商品名称
            _log.info(logPrefix + "******************sign:{}", sign);
            System.out.println(map.toString());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Index.html", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            Map<String,String> mapNow=XXPayUtil.formBuildByMap(res);
            String payUrl=mapNow.get("action");
            mapNow.remove("action");
            String resNow= XXPayUtil.http(payUrl,mapNow);
            JSONObject resObj = JSONObject.parseObject(resNow);
            String payDayuUrl="";
            JSONObject payParams = new JSONObject();
            switch (pay_code)
            {
                case  "901":
                    if (!resObj.getString("code").equals("1"))
                    {
                        payInfo.put("errDes", "下单失败[" +resObj.getString("msg") + "]");
                        payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                        return payInfo;
                    }
                    payDayuUrl=resObj.getString("url");
                    break;
                case  "904":
                    if (resObj.getString("retCode").equals("SUCCESS")) {
                        String payParams1 =resObj.getString("payParams");
                        JSONObject jsonObject1 = JSONObject.parseObject(payParams1);
                        payDayuUrl = URLDecoder.decode(jsonObject1.getString("sdkUrl"),"UTF-8");

                    } else {
                        payInfo.put("errDes", "下单失败[" + res + "]");
                        payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                        return payInfo;
                    }
                    break;
            }

            payParams.put("payJumpUrl", payDayuUrl);
            if (payOrder.getChannelId().contains("SDK")) {
                if (payDayuUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                }
            } else {
                if (payDayuUrl.contains("form")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
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
        String logPrefix = "【环亚支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户号
            map.put("pay_orderid", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);//订单号
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/index/order_query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("returncode");
            if (code.equals("0000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("memberid", resObj.getString("memberid"));//商户编号
                map1.put("orderid", resObj.getString("orderid"));//平台订单号
                map1.put("amount", resObj.getString("amount"));//订单状态
                map1.put("time_end", resObj.getString("time_end"));//订单实际金额
                map1.put("transaction_id", resObj.getString("transaction_id"));//状态码
                map1.put("returncode", resObj.getString("returncode"));//返回code
                map1.put("trade_state", resObj.getString("trade_state"));//交易状态
                System.out.println("环亚------》》》》》》《《《《《《++++++" + map1.toString());
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key()).toLowerCase();
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
                if (resObj.getString("trade_state").equals("SUCCESS")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
            }
            String status = retObj.getString("status");
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
