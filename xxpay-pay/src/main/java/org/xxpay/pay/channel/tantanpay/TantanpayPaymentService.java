package org.xxpay.pay.channel.tantanpay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
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
public class TantanpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(TantanpayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_TANTAN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doTantanpayRyPayReq(payOrder, "1"); //支付宝H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_PDDSDK:
                retObj = doTantanpayRyPayReq(payOrder, "20"); //支付宝SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doTantanpayRyPayReq(payOrder, "13"); //微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doTantanpayRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【泰坦支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("amount", String.valueOf(amount));// 金额
            map.put("client_ip","127.0.0.1");// 版本号
            map.put("goods_name","商品名称");//商品名称
            map.put("is_form","2");//1：form表单跳转提交，返回html; 2：返回Json；
            map.put("merchant_id", channelPayConfig.getMchId());//商户号
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("out_trade_id", payOrder.getPayOrderId());// 商户订单号
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("product_id",pay_code);//支付类型
            map.put("return_url",payConfig.getReturnUrl(channelName));//同步返回
            map.put("attach","韩新四方");//附加信息
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "下单验签串sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************请求地址:{}", channelPayConfig.getReqUrl() + "/pay/gateway/create_order");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/gateway/create_order", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("1")) {
                JSONObject payParams = new JSONObject();
                String  payJumpUrl = resObj.getJSONObject("data").getString("pay_url");
                if (channelPayConfig.getChannelId().equals("20"))
                {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                }else {
                    //表单跳转
                    if (payJumpUrl.contains("<html")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }
                payParams.put("payJumpUrl", payJumpUrl);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + res + "]");
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
        String logPrefix = "【泰坦支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("out_trade_id", payOrder.getPayOrderId());// 订单号
            map.put("merchant_id", channelPayConfig.getMchId()); // 商户号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/gateway/query_order", jsonObj.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");

            if (code.equals("0000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("amount", resObj.getString("amount"));//金额
                map1.put("attach", resObj.getString("attach"));//备注
                map1.put("create_time", resObj.getString("create_time"));//订单时间
                map1.put("goods_name", resObj.getString("goods_name"));//商品名称
                map1.put("merchant_id", resObj.getString("merchant_id"));//商户id
                map1.put("out_trade_id", resObj.getString("out_trade_id"));//商户订单号
                map1.put("pay_time", resObj.getString("pay_time"));//时间
                map1.put("status", resObj.getString("status"));//状态
                map1.put("transaction_id", resObj.getString("transaction_id"));//交易流水号
                System.out.println("泰坦------》》》》》》《《《《《《++++++" + map1.toString());
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key()).toLowerCase();
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            }
            if (resObj.getString("status").equals("2")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            String status=resObj.getString("status");
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
            case "1":
                return "支付成功";
            case "2":
                return "支付异常";
            case "3":
                return "未支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
