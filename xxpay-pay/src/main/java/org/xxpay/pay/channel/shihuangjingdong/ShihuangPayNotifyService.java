package org.xxpay.pay.channel.shihuangjingdong;//

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;


import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ShihuangPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(ShihuangPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {

        //回调 进入 始皇京东E卡

        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
//        retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_SUCCESS);
//        return retObj;
        String logPrefix = "【处理三国支付回调】";
        _log.info("====== 开始处理三国支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        _log.info("始皇京东E卡回调类接收参数：：{}", request.toString());
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("orderid", request.getParameter("orderid"));
            params.put("m_orderid", request.getParameter("m_orderid"));
            params.put("payamount", request.getParameter("payamount"));
            params.put("sign", request.getParameter("sign"));
            _log.info("始皇京东E卡============={}" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows=0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
//            if ("00".equals(params.get("returncode"))) {
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
//            }
            // 业务系统后端通知
            if (updatePayOrderRows>0)
            {
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理三国支付回调通知 ======");
            respString = "success";
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证三国支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String resign = params.getString("sign");                //签名
        String amount = params.getString("payamount");                    // 订单金额
        // 查询payOrder记录
        String payOrderId = params.getString("m_orderid");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        params.remove("sign");
        Map signMap = new HashMap<>();
        String signValue = params.getString("orderid") + params.getString("m_orderid") + params.getString("payamount") + channelPayConfig.getmD5Key();
        signValue = PayDigestUtil.md5(signValue, "UTF-8");
        if (!resign.toUpperCase().equals(signValue.toUpperCase())) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long outPayAmt = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();//乘以100
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }
}
