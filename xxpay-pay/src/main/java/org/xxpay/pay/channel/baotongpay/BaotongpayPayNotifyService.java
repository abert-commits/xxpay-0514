package org.xxpay.pay.channel.baotongpay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.axiang.AxiangpayPaymentService;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BaotongpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(BaotongpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_BAOTONG;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理宝通支付回调】";
        _log.info("====== 开始处理宝通支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        System.out.println("宝通下单回调参数??>>><<<<<++++++?>>>>" + request.getParameterMap());
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("code", jsonParam.getString("code"));
            params.put("msg", jsonParam.getString("msg"));
            params.put("merchNo", jsonParam.getString("merchNo"));
            params.put("amount", jsonParam.getString("amount"));
            params.put("tradeNo", jsonParam.getString("tradeNo"));
            params.put("orderNo", jsonParam.getString("orderNo"));
            params.put("status", jsonParam.getString("status"));
            params.put("remark", jsonParam.getString("remark"));
            params.put("sign", jsonParam.getString("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            int updatePayOrderRows = 0;
            System.out.println("回调订单号++++《《《《》》》》》"+payOrder.getPayOrderId());
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
            if (updatePayOrderRows == 1) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }
            _log.info("====== 完成处理宝通支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证宝通支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String code = params.getString("code");  //状态值 ： CODE_SUCCESS 成功， CODE_FAILURE 失败
        String msg = params.getString("msg"); // 支付方式
        String merchNo = params.getString("merchNo"); // 支付总额
        String amount = params.getString("amount");   //客户端订单编号
        String tradeNo = params.getString("tradeNo"); //拼多多订单编号
        String orderNo = params.getString("orderNo");

        String status = params.getString("status"); //拼多多订单编号
        String remark = params.getString("remark"); //拼多多订单编号
        String sign = params.getString("sign"); //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(tradeNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", tradeNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, Object> signMap = new TreeMap<String, Object>();
        signMap.put("code", code);
        signMap.put("msg", msg);
        signMap.put("merchNo",merchNo);
        signMap.put("amount", amount);
        signMap.put("tradeNo", tradeNo);
        signMap.put("orderNo", orderNo);
        signMap.put("status", status);
        signMap.put("remark", remark);
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", tradeNo);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, tradeNo);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
