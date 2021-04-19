package org.xxpay.pay.channel.taibai;

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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class LibaipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(LibaipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LIBAI;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix ="【处理李白支付回调】";
        _log.info("====== 开始处理李白支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("orderId", retObj.getString("orderId"));
            params.put("thirdOrderId", retObj.getString("thirdOrderId"));
            params.put("status", retObj.getString("status"));
            params.put("money", retObj.getString("money"));
            params.put("userId", retObj.getString("userId"));
            params.put("tpUserId", retObj.getString("tpUserId"));
            params.put("type", retObj.getString("type"));
            params.put("cName", retObj.getString("cName"));
            params.put("rejectInfo", retObj.getString("rejectInfo"));
            params.put("amount", retObj.getString("amount"));
            params.put("bearer", retObj.getString("bearer"));
            params.put("timestamp", retObj.getString("timestamp"));
            params.put("sign", retObj.getString("sign"));

            _log.info("李白付支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
            if ("2".equals(params.get("status"))) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                    if (updatePayOrderRows != 1) {
                        _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                        retObj.put(PayConstant.RESPONSE_RESULT,"处理订单失败");
                        return retObj;
                    }
                    _log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                }
            }
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理李白付支付回调通知 ======");
            respString ="OK";
        } catch (Exception e) {
            _log.error(e, logPrefix +"处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证JQK支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws UnsupportedEncodingException {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String orderId = params.getString("orderId");                // otc平台订单id

        String thirdOrderId = params.getString("thirdOrderId");                    //  商户，即第三方平台的订单id

        String status = params.getString("status");                //订单状态：1.待付款或者还原（针对于有超时取消 的订单）;2.已付款;3.确认收款;4.取消        (Tip:充值 订单：目前节省了用户点击已付款操作，所以下单 即已付款，1=2。只需要商家那边点击确认收款即 可)
        String money = params.getString("money");                    // 订单总金额 例如：100.00 单位：元，保留两位小 数，否则签名验证可能会出错
        String userId = params.getString("userId");               // 商户，即第三方平台的用户id
        String tpUserId = params.getString("tpUserId");                    //  支付平台的用户id
        String type = params.getString("type");                //.买入订单，2.卖出订单
        String cName = params.getString("cName");                    //  币种名称，用户承担时才会返回
        String rejectInfo = params.getString("rejectInfo");                //只有订单状态为4的时候，才可能有值，表示取消 的原
        String amount = params.getString("amount");                    // 币种数量，用户承担时才有返回
        String bearer = params.getString("bearer");                    // 商户承担，1.用户承担
        String timestamp = params.getString("timestamp");                    //  0.通知时的时间戳，毫秒
        String sign = params.getString("sign");                    // 订单金额
        // 查询payOrder记录
        String payOrderId = params.getString("orderid");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={},", payOrderId);
            payContext.put("retMsg","Can't found payOrder");
            return false;
        }
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        String getmD5Key = channelPayConfig.getRsapassWord();//订单详情

        // 验证签名
        params.remove("sign");
        params.remove("amount");
        params.remove("rejectInfo");
        String md5 =LibaipayPaymentService.getSign(params, "","");
        if (!md5.toUpperCase().equals(sign.toUpperCase())) {
            _log.error("验证签名失败. payOrderId={},", payOrderId);
            payContext.put("retMsg","验证签名失败");
            return false;
        }


        if (Long.valueOf(money)!= Long.valueOf(AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg","金额不一致");
            return false;
        }


        payContext.put("payOrder", payOrder);
        return true;
    }
}
