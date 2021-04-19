package org.xxpay.pay.channel.jiandanpay;

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
public class JiandanpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(JiandanpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JIANDAN;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理简单付支付回调】";
        _log.info("====== 开始处理简单付支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("callbacks", request.getParameter("callbacks"));
            params.put("appid", request.getParameter("appid"));
            params.put("pay_type", request.getParameter("pay_type"));
            params.put("success_url", request.getParameter("success_url"));
            params.put("error_url", request.getParameter("error_url"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("amount", request.getParameter("amount"));
            params.put("amount_true", request.getParameter("amount_true"));
            params.put("out_uid", request.getParameter("out_uid"));
            params.put("sign", request.getParameter("sign"));
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
            _log.info("====== 完成处理简单付支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证简单支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String callbacks = params.getString("callbacks");  //商户号
        String appid = params.getString("appid"); //商户订单号
        String pay_type = params.getString("pay_type"); //支付金额
        String success_url = params.getString("success_url"); //随机金额
        String error_url = params.getString("error_url"); //状态值
        String out_trade_no = params.getString("out_trade_no"); //状态值
        String amount = params.getString("amount"); //状态值
        String amount_true = params.getString("amount_true"); //状态值
        String out_uid = params.getString("out_uid"); //状态值


        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(out_trade_no);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("callbacks", callbacks);
        signMap.put("appid", appid);
        signMap.put("pay_type", pay_type);
        signMap.put("success_url", success_url);
        signMap.put("error_url", error_url);
        signMap.put("out_trade_no", out_trade_no);
        signMap.put("amount", amount);
        signMap.put("amount_true", amount_true);
        signMap.put("out_uid", out_uid);

        String md52 = PayDigestUtil.getSign(signMap,channelPayConfig.getmD5Key());
        if (!md52.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrder.getPayOrderId());
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long outPayAmt = new BigDecimal(amount_true).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrder.getPayOrderId());
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
