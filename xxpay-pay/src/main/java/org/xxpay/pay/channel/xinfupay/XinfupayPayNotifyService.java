package org.xxpay.pay.channel.xinfupay;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.*;

@Service
public class XinfupayPayNotifyService extends BasePayNotify {
    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XINFU;
    }

    private static final MyLog _log = MyLog.getLog(XinfupayPaymentService.class);

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理新富支付回调】";
        _log.info("====== 开始处理新富支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        String paraName = "";
        Enumeration enu=request.getParameterNames();
        while(enu.hasMoreElements()){
             paraName=(String)enu.nextElement();
            System.out.println(paraName+": "+request.getParameter(paraName));
        }
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        StringBuilder jb = new StringBuilder();
        try {
            JSONObject jsonObject=JSONObject.parseObject(paraName);
            request.setCharacterEncoding("UTF-8");
            String rsp = CommonUtils.ReadAsChars(request);
            _log.info("支付回调=============" + rsp);
            JSONObject params = new JSONObject();
            params.put("accessKey", jsonObject.getString("accessKey"));
            params.put("orderId", jsonObject.getString("orderId"));
            params.put("outTradeNo", jsonObject.getString("outTradeNo"));
            params.put("money", jsonObject.getString("money"));
            params.put("timestamp", jsonObject.getString("timestamp"));
            params.put("signature", jsonObject.getString("signature"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");

            int updatePayOrderRows = 0;
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理新富支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证新富支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String accessKey = params.getString("accessKey");  //accessKey
        String amount = params.getString("money"); // 订单金额
        String outOrderNo = params.getString("outTradeNo"); // 商户订单号
        String sys_order_no = params.getString("orderId"); // 平台流水号
        String timestamp = params.getString("timestamp"); //订单请求时间
        String signature = params.getString("signature");  //签名
        // 查询payOrder记录
        String payOrderId = outOrderNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("accessKey", accessKey);
        signMap.put("money", amount);
        signMap.put("orderId", sys_order_no);
        signMap.put("outTradeNo", outOrderNo);
        signMap.put("timestamp", timestamp);

        String SignMsg = XXPayUtil.mapToString(signMap).replace(">", "");
        String md5 = HmachshaUtil.createSign(channelPayConfig.getmD5Key(), signMap);
//        String md5 = Base64Utils.encode(signs.getBytes());
        if (!md5.equals(signature)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(amount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }


}
