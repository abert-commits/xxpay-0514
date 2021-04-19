package org.xxpay.pay.channel.qihu;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class QihuPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(QihuPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理七虎支付回调】";
        _log.info("====== 开始处理七虎支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {


//            JSONObject params =JSONObject.parseObject(GetInput(notifyData));
            JSONObject params = GetParamsToJSONObject(request);
            _log.info("七虎支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }


            payOrder = (PayOrder) payContext.get("payOrder");
            boolean flag = CheckCallIP(request, payOrder.getPassageId(), payOrder);
            if (!flag) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return retObj;
            }

            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
//            if ("1".equals(params.get("status").toString())) {
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
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理七虎支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证云支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws IllegalAccessException {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String resign = params.getString("sign");                //签名
        String amount = params.getString("amount");                    // 订单金额
        // 查询payOrder记录
        String payOrderId = params.getString("out_trade_no");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }
        params.remove("sign");
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        Map<String, String> map = XXPayUtil.JSONObjectToSortedMap(params);
        String strSign = XXPayUtil.mapToString(map) + "&secret_key=" + channelPayConfig.getmD5Key();
        String signRes = PayDigestUtil.md5(strSign, "UTF-8");
        if (!resign.equals(signRes)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != new BigDecimal(amount).multiply(new BigDecimal(100)).longValue()) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }

        payContext.put("payOrder", payOrder);
        return true;
    }
}
