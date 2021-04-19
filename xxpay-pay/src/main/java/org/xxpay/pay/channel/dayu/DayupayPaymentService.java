package org.xxpay.pay.channel.dayu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
public class DayupayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(DayupayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAYU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XJ:
                retObj = doJuheRyPayReq(payOrder, "100"); //支付宝现金红包
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doJuheRyPayReq(payOrder, "300"); //支付宝扫码
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJuheRyPayReq(payOrder, "0020"); //支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【大宇支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {



            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            if (!StringUtils.isBlank(channelPayConfig.getChannelId())) {
                pay_code=channelPayConfig.getChannelId();
            }
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("appid", channelPayConfig.getMchId());//商户号
            map.put("version", "1.1");//商户号
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            map.put("total_fee", String.valueOf(amount));// 金额
            map.put("pay_id", pay_code); //支付编码

            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("return_url", payConfig.getReturnUrl(channelName)); //支付完成返回地址
            String sign = PayDigestUtil.getSignNotKey(map, channelPayConfig.getmD5Key()).toLowerCase();

            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/index/tradePay",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("0000")) {
                String paydata= resObj.getString("data");
                JSONObject jsonObject1 = JSONObject.parseObject(paydata);
                String payDayuUrl = jsonObject1.getString("url");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payDayuUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payDayuUrl.contains("form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【大宇支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("appid", channelPayConfig.getMchId());//商户号
            map.put("version", "1.1");
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);//订单号
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/index/orderQuery",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString(  "code");
            if (code.equals("0000")) {
                String data = resObj.getString("data");
                JSONObject jsonObject = JSONObject.parseObject(data);
                if (jsonObject.getString("status").equals("1")) {
                    retObj.put("status", "2");
                } else {
                    retObj.put("status", "1");
                }
            }
            String order_status=retObj.getString("order_status");
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + order_status + "");
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
