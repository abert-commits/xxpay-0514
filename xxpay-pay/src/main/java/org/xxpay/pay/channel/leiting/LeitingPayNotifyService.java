package org.xxpay.pay.channel.leiting;

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
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class LeitingPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(LeitingPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LEITING;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理雷霆支付回调】";
        _log.info("====== 开始处理雷霆支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("mch_id", request.getParameter("mch_id"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("order_no", request.getParameter("order_no"));
            params.put("pay_time", request.getParameter("pay_time"));
            params.put("timestamp", request.getParameter("timestamp"));
            params.put("total_fee", request.getParameter("total_fee"));
        

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
            if ("2".equals(params.get("status")) || "3".equals(params.get("status"))) {
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

            if (updatePayOrderRows==1)
            {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理雷霆支付回调通知 ======");
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
        String mch_id	= params.getString("mch_id");  //商户UID
        String out_trade_no = params.getString("out_trade_no"); //商户创建订单时请求的订单号
        String order_no	 = params.getString("order_no"); //商户创建订单时返回的订单号
        String pay_time	 = params.getString("pay_time");   //支付时间
        String timestamp = params.getString("timestamp"); //当前时间戳
        String total_fee = params.getString("total_fee"); //订单金额
      

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
        SortedMap<String, Object> signMap = new TreeMap<String, Object>();
        signMap.put("mch_id", mch_id);
        signMap.put("out_trade_no", out_trade_no);
        signMap.put("order_no", order_no);
        signMap.put("pay_time", pay_time);
        signMap.put("timestamp", timestamp);
        signMap.put("total_fee", total_fee);
        signMap.put("mch_secret", channelPayConfig.getmD5Key());
        // md5 加密
        String md5 = PayDigestUtil.getSignNotKey(signMap, "");
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(total_fee))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total_fee, out_trade_no);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
