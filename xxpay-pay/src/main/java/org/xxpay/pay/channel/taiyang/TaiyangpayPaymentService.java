package org.xxpay.pay.channel.taiyang;

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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class TaiyangpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(TaiyangpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_TAIYANG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doTaiYangPayReq(payOrder, "27");   //支付宝 SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doTaiYangQRPayReq(payOrder, "25");   //微信 H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doTaiYangPayReq(payOrder, "6");   //支付宝 扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    /**
     * 扫码
     *
     * @param payOrder
     * @param channelId
     * @return
     */
    public JSONObject doTaiYangQRPayReq(PayOrder payOrder, String channelId) {
        String logPrefix = "【太阳支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());// 商家号
            map.put("pay_orderid", payOrder.getPayOrderId()); //商户订单号
            map.put("pay_applydate", DateUtil.getCurrentTimeStr(DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); //提交时间
            map.put("pay_bankcode", channelId); //通道编码
            map.put("pay_notifyurl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("pay_callbackurl", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_amount", amount); //支付金额
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign); //签名
            map.put("pay_productname", "韩信支付"); //商品描述信息
            StringBuilder sb = new StringBuilder();

            sb.append("<form id='submit' name='submit' action='"+channelPayConfig.getReqUrl() + "/index/pay' method='post'>");
            sb.append("<input type='hidden' name='pay_memberid' value='"+map.get("pay_memberid")+"'/>");
            sb.append("<input type='hidden' name='pay_orderid' value='"+ map.get("pay_orderid")+"'/>");
            sb.append("<input type='hidden' name='pay_applydate' value='"+map.get("pay_applydate")+"'/>");
            sb.append("<input type='hidden' name='pay_bankcode' value='"+map.get("pay_bankcode")+"'/>");
            sb.append("<input type='hidden' name='pay_notifyurl' value='"+map.get("pay_notifyurl")+"'/>");
            sb.append("<input type='hidden' name='pay_callbackurl' value='"+map.get("pay_callbackurl")+"'/>");
            sb.append("<input type='hidden' name='pay_amount' value='"+map.get("pay_amount")+"'/>");
            sb.append("<input type='hidden' name='pay_md5sign' value='"+map.get("pay_md5sign")+"'/>");
            sb.append("<input type='hidden' name='pay_productname' value='"+map.get("pay_productname")+"'/>");

            sb.append("<input type='submit' value='提交'>");
            sb.append("</form>");
            sb.append("<script>document.forms['submit'].submit();</script>");

            _log.info(logPrefix + "******************sendMsg:{}", sb.toString());
            JSONObject payParams = new JSONObject();
            //表单跳转
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl", sb.toString());
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


    public JSONObject doTaiYangPayReq(PayOrder payOrder, String channelId) {
        String logPrefix = "【太阳支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());// 商家号
            map.put("pay_orderid", payOrder.getPayOrderId()); //商户订单号
            map.put("pay_applydate", DateUtil.getCurrentTimeStr(DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); //提交时间
            map.put("pay_bankcode", channelId); //通道编码
            map.put("pay_notifyurl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("pay_callbackurl", payConfig.getReturnUrl(getChannelName())); //支付结果后台回调URL
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("pay_amount", amount); //支付金额

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign); //签名
            map.put("pay_productname", "韩信支付"); //商品描述信息

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/index/pay ", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("errcode");
            String retMsg = resObj.getString("errmsg");

            if ("success".equals(retMsg)) {
                String payJumpUrl = URLDecoder.decode(resObj.getString("sdk"), "UTF-8");
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
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【太阳支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());// 商家号
            map.put("pay_orderid", payOrder.getPayOrderId()); //商户订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/trade/query ", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String returncode = resObj.getString("returncode");

            if (returncode.equals("00")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + returncode + ",订单状态:" + GetStatusMsg(resObj.getString("trade_state")));
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
            case "SUCCESS":
                return "订单生成";
            case "NOTPAY":
                return "未支付";
            default:
                return "交易失败";
        }
    }
}
