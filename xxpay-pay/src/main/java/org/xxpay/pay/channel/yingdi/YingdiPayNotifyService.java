package org.xxpay.pay.channel.yingdi;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class YingdiPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(YingdiPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理影帝支付回调】";
        _log.info("====== 开始处理影帝支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        //获取到JSONObject
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();


            params.put("timestamp", jsonParam.getString("timestamp"));
            params.put("mid", jsonParam.getString("mid"));
            params.put("noncestr", jsonParam.getString("noncestr"));
            params.put("sign", jsonParam.getString("sign"));

            JSONObject date=JSONObject.parseObject(jsonParam.getString("data"));

            params.put("orderid", date.getString("orderid"));
            params.put("plat_orderid", date.getString("plat_orderid"));
            params.put("price", date.getString("price"));
            params.put("paystatus", date.getString("paystatus"));
            params.put("payment", date.getString("payment"));
            params.put("paytime", date.getString("paytime"));

            params.put("data", jsonParam.getString("data"));

            _log.info("影帝支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
            if ("1".equals(params.get("paystatus"))) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                    if (updatePayOrderRows != 1) {
                        _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                        retObj.put(PayConstant.RESPONSE_RESULT, "处理订单失败");
                        return retObj;
                    }
                    _log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                }
            }
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理影帝支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证JQK支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String mid = params.getString("mid");   //
        String timestamp = params.getString("timestamp");   //
        String noncestr = params.getString("noncestr");   //

        String data = params.getString("data");     //
        String orderid = params.getString("orderid");    //商户订单号
        String plat_orderid = params.getString("plat_orderid");   //平台订单号
        String price = params.getString("price");     //金额
        String paystatus = params.getString("paystatus");  //支付状态
        String payment = params.getString("payment");
        String paytime = params.getString("paytime");   //支付时间




        String resign = params.getString("sign");                //签名
        // 查询payOrder记录
        String payOrderId = params.getString("orderid");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("mid", mid);
        jsonObject.put("timestamp",timestamp);
        jsonObject.put("noncestr",noncestr);

        LinkedHashMap jsonObject1=new LinkedHashMap();
        jsonObject1.put("orderid",orderid);
        jsonObject1.put("plat_orderid",plat_orderid);
        jsonObject1.put("price", price);
        jsonObject1.put("paystatus",paystatus);
        jsonObject1.put("payment",payment);
        jsonObject1.put("paytime",paytime);
        jsonObject.put("data", data);

        String sign = PayDigestUtil.getSign(jsonObject, channelPayConfig.getmD5Key());

        if (!resign.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(price))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", price, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }


        payContext.put("payOrder", payOrder);
        return true;
    }
}
