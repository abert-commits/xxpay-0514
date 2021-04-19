package org.xxpay.pay.channel.afanda;

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
public class AfandaPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(AfandaPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_AFANDA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理阿凡达支付回调】";
        _log.info("====== 开始处理阿凡达支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("merchant", request.getParameter("merchant"));
            params.put("merchant_money", request.getParameter("merchant_money"));
            params.put("qrtype", request.getParameter("qrtype"));
            params.put("customno", request.getParameter("customno"));
            params.put("sendtime", request.getParameter("sendtime"));
            params.put("orderno", request.getParameter("orderno"));
            params.put("money", request.getParameter("money"));
            params.put("paytime", request.getParameter("paytime"));
            params.put("state", request.getParameter("state"));
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

            if (updatePayOrderRows==1) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理阿凡达支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证环亚支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String merchant = params.getString("merchant");  //商户号
        String merchant_money = params.getString("merchant_money"); // 提交金额
        String qrtype = params.getString("qrtype"); // 类型
        String customno = params.getString("customno"); //商户订单号
        String sendtime = params.getString("sendtime");   //订单时间
        String orderno = params.getString("orderno"); //支付平台订单号
        String money = params.getString("money"); //订单金额
        String paytime = params.getString("paytime"); //客户支付时间
        String state = params.getString("state"); //订单状态
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(customno);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", customno);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        String str = String.format("merchant=%s&merchant_money=%s&qrtype=%s&customno=%s&sendtime=%s&orderno=%s&money=%s&paytime=%s&state=%s",
                merchant, merchant_money, qrtype, customno, sendtime, orderno, money, paytime, state);
        // md5 加密
        String md5 = PayDigestUtil.md5((str+channelPayConfig.getmD5Key()), "UTF-8").toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", customno);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(money).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, customno);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
