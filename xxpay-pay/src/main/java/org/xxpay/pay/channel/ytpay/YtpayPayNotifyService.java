package org.xxpay.pay.channel.ytpay;

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
public class YtpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(YtpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YT;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理YT支付回调】";
        _log.info("====== 开始处理YT支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);

        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("fxid", request.getParameter("fxid"))    ;
            params.put("fxorderid", request.getParameter("fxorderid"));
            params.put("fxtranid", request.getParameter("fxtranid"));
            params.put("fxamount", request.getParameter("fxamount"));
            params.put("fxamount_succ", request.getParameter("fxamount_succ"));
            params.put("fxstatus", request.getParameter("fxstatus"));
            params.put("fxremark",request.getParameter("fxremark"));
            params.put("fxsign", request.getParameter("fxsign"));
            _log.info("YT支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
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
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理YT支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证易宝话费支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        BigDecimal b = new BigDecimal(params.getString("fxamount"));
        String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        // 校验结果是否成功
        String memberid	 = params.getString("fxid");  //商户编号
        String orderid = params.getString("fxorderid"); // 订单号

        String transaction_id = params.getString("fxtranid"); //交易流水号
//        BigDecimal a = new BigDecimal(params.getString("fxamount_succ"));
//        String fxamount_succ = a.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String fxamount_succ = params.getString("fxamount_succ"); //实际支付金额
        String fxstatus = params.getString("fxstatus"); //交易状态
        String fxremark = params.getString("fxremark"); //交易状态
        String sign = params.getString("fxsign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(orderid);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", orderid);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        String md5 = PayDigestUtil.md5(fxstatus + memberid + fxamount_succ + orderid+ amount + channelPayConfig.getmD5Key(),"UTF-8");
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", orderid);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(fxamount_succ).multiply(new BigDecimal(100)).longValue();
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, orderid);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }
}
