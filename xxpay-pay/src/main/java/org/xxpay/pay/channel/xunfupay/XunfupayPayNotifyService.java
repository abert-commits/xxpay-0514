package org.xxpay.pay.channel.xunfupay;

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
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class XunfupayPayNotifyService extends BasePayNotify {
    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XUNFUPAY;
    }

    private static final MyLog _log = MyLog.getLog(XunfupayPaymentService.class);

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理迅付支付回调】";
        _log.info("====== 开始处理迅付支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        System.out.println("打印总参数++++————————《《《》》"+request.getParameterMap());
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("amount", request.getParameter("amount"));
            params.put("attach", request.getParameter("attach"));
            params.put("create_time", request.getParameter("create_time"));
            params.put("goods_name", request.getParameter("goods_name"));
            params.put("merchant_id", request.getParameter("merchant_id"));
            params.put("out_trade_id", request.getParameter("out_trade_id"));
            params.put("pay_time", request.getParameter("pay_time"));
            params.put("status", request.getParameter("status"));
            params.put("transaction_id", request.getParameter("transaction_id"));
            params.put("sign", request.getParameter("sign"));
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

            _log.info("====== 完成处理迅付支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证迅付支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String amount = params.getString("amount"); // 订单金额
        BigDecimal b = new BigDecimal(params.getString("amount"));
        amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String attach = params.getString("attach");//版本号
        String create_time = params.getString("create_time");  //商户号
        String goods_name = params.getString("goods_name"); // 我方流水号
        String merchant_id = params.getString("merchant_id"); // 订单号
        String out_trade_id = params.getString("out_trade_id"); //订单请求时间
        String pay_time = params.getString("pay_time"); //订单状态
        String status    = params.getString("status");//签名方法
        String transaction_id = params.getString("transaction_id");
        String sign = params.getString("sign");  //签名
        // 查询payOrder记录
        String payOrderId = out_trade_id;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("amount", amount);
        signMap.put("attach",attach);
        signMap.put("create_time", create_time);
        signMap.put("goods_name", goods_name);
        signMap.put("merchant_id", merchant_id);
        signMap.put("out_trade_id", out_trade_id);
        signMap.put("pay_time", pay_time);
        signMap.put("status", status);
        signMap.put("transaction_id", transaction_id);

        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }


}
