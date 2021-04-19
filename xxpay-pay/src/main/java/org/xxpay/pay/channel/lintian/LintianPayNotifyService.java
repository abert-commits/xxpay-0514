package org.xxpay.pay.channel.lintian;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class LintianPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(LintianPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SHUNAN;
    }


    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理麟天支付回调】";
        _log.info("====== 开始处理麟天支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();

            params.put("merchantNum", request.getParameter("merchantNum"));
            params.put("orderNo", request.getParameter("orderNo"));
            params.put("platformOrderNo", request.getParameter("platformOrderNo"));
            params.put("amount", request.getParameter("amount"));
            params.put("attch", request.getParameter("attch"));
            params.put("state", request.getParameter("state"));
            params.put("payTime", request.getParameter("payTime"));
            params.put("sign", request.getParameter("sign"));
            params.put("actualPayAmount", request.getParameter("actualPayAmount"));
            _log.info("支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if ("1".equals(params.get("state"))) {
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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }
            _log.info("====== 完成处理麟天支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String order_no = params.getString("platformOrderNo");  //支付订单号
        String out_order_no = params.getString("orderNo"); // 商户订单号
        String amount = params.getString("amount");   //交易金额
        String sign = params.getString("sign");  //签名
        String state = params.getString("state");
        String merchantNum = params.getString("merchantNum");

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(out_order_no);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", out_order_no);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        String md5 = PayDigestUtil.md5(state + merchantNum + out_order_no + amount + channelPayConfig.getmD5Key(), "UTF-8");

        if (!md5.toUpperCase().equals(sign.toUpperCase())) {
            _log.error("验证签名失败. payOrderId={}, ", out_order_no);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, out_order_no);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
