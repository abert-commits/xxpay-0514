package org.xxpay.pay.channel.sanye;

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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SanyePaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(SanyePaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SANYE;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doTengAnPayReq(payOrder, "");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doTengAnPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【三叶支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户号
            map.put("pay_orderid", payOrder.getPayOrderId());//订单号
            map.put("pay_applydate", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); //请求时间
            map.put("pay_bankcode", channelPayConfig.getChannelId()); //银行编码
            map.put("pay_notifyurl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("pay_callbackurl", payConfig.getReturnUrl(channelName)); //页面跳转返回地址
            map.put("pay_amount", String.valueOf(amount));// 金额

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            map.put("pay_md5sign", sign);// md5签名
            map.put("pay_productname", "三叶支付");//商品名称
            _log.info(logPrefix + "******************sign:{}", sign);
            System.out.println(map.toString());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String payUrl =  XXPayUtil.buildRequestHtml(map,channelPayConfig.getReqUrl() + "/Pay_Index.html");
            _log.info(logPrefix + "******************上游返回数据:{}", sendMsg);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(),null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            JSONObject payParams = new JSONObject();
            payParams.put("payMethod",PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl",payUrl);
            payInfo.put("payParams",payParams);
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【三叶支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户号
            map.put("pay_orderid", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Trade_query.html",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("returncode");
            if (code.equals("00")) {
                String ressign = resObj.getString("sign").toLowerCase();
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("memberid", resObj.getString("memberid"));//商户编号
                map1.put("orderid", resObj.getString("orderid"));//平台订单号
                map1.put("amount", resObj.getString("amount"));//订单金额
                map1.put("time_end", resObj.getString("time_end"));//支付成功时间
                map1.put("transaction_id", resObj.getString("transaction_id"));//交易流水号
                map1.put("returncode", resObj.getString("returncode"));//交易状态
                map1.put("trade_state", resObj.getString("trade_state"));//交易状态
                System.out.println("三叶------》》》》》》《《《《《《++++++" + map1.toString());
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
                String status=resObj.getString("trade_state");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            } else {
                retObj.put("errDes", "查询失败!");
                retObj.put("msg", "查询系统：请求上游查询失败！");
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return retObj;
            }
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "SUCCESS":
                return "订单生成";
            case "NOTPAY":
                return "未支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
