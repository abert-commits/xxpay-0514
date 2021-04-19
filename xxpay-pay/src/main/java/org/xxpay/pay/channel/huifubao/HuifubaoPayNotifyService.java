package org.xxpay.pay.channel.huifubao;

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
public class HuifubaoPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HuifubaoPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YIBAOHUAFEI;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理汇付宝话费支付回调】";
        _log.info("====== 开始处理汇付宝话费支付回调通知 ======");
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
            params.put("version",request.getParameter("version"));
            params.put("memberid", request.getParameter("memberid"));
            params.put("orderid", request.getParameter("orderid"));
            params.put("amount", request.getParameter("amount"));
            params.put("datetime", request.getParameter("datetime"));
            params.put("transaction_id", request.getParameter("transaction_id"));
            params.put("actual_amount", request.getParameter("actual_amount"));
            params.put("returncode",request.getParameter("returncode"));
            params.put("sign", request.getParameter("sign"));
            _log.info("汇付宝话费支付回调=============" + params);

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

            _log.info("====== 完成处理汇付宝话费支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK_1;
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
        BigDecimal b = new BigDecimal(params.getString("amount"));
        String amount = b.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        // 校验结果是否成功
        String version = params.getString("version");//版本号
        String memberid	 = params.getString("memberid");  //商户编号
        String orderid = params.getString("orderid"); // 订单号

        BigDecimal c = new BigDecimal(params.getString("actual_amount"));
        String actual_amount = c.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        String transaction_id = params.getString("transaction_id"); //交易流水号
        String datetime = params.getString("datetime"); //交易时间
        String returncode = params.getString("returncode"); //交易状态
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(orderid);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", orderid);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("version",version);
        signMap.put("memberid", memberid);
        signMap.put("orderid", orderid);
        signMap.put("amount", amount);
        signMap.put("actual_amount", actual_amount);
        signMap.put("transaction_id", transaction_id);
        signMap.put("datetime", datetime);
        signMap.put("returncode", returncode);
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getRsaPublicKey());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", orderid);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(actual_amount).multiply(new BigDecimal(100)).longValue();//乘以100
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
