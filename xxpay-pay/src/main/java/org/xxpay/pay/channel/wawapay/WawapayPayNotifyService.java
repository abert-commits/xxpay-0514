package org.xxpay.pay.channel.wawapay;

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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class WawapayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(WawapayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理娃娃1支付回调】";
        _log.info("====== 开始处理娃娃1支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("payOrderId", request.getParameter("payOrderId"));
            params.put("mchId", request.getParameter("mchId"));
            params.put("appId", request.getParameter("appId"));
            params.put("productId", request.getParameter("productId"));
            params.put("mchOrderNo", request.getParameter("mchOrderNo"));
            params.put("amount", request.getParameter("amount"));
            params.put("income", request.getParameter("income"));
            params.put("status", request.getParameter("status"));
            params.put("channelOrderNo", request.getParameter("channelOrderNo"));
            params.put("channelAttach", request.getParameter("channelAttach"));
            params.put("param1", request.getParameter("param1"));
            params.put("param2", request.getParameter("param2"));
            params.put("paySuccTime", request.getParameter("paySuccTime"));
            params.put("backType", request.getParameter("backType"));


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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }
            _log.info("====== 完成处理娃娃1支付回调通知 ======");
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
        String payOrderId = params.getString("payOrderId");  //支付订单号
        String mchId = params.getString("mchId"); // 商户ID
        String appId = params.getString("appId"); // 应用ID
        String productId = params.getString("productId");   //支付产品ID
        String mchOrderNo = params.getString("mchOrderNo"); //商户订单号
        String amount = params.getString("amount"); //支付金额
        String income = params.getString("income"); //入账金额
        String status = params.getString("status"); //状态
        String channelOrderNo = params.getString("channelOrderNo"); //渠道订单号
        String channelAttach = params.getString("channelAttach"); //渠道数据包
        String param1 = params.getString("param1"); //扩展参数1
        String param2 = params.getString("param2"); //扩展参数2
        String paySuccTime = params.getString("paySuccTime"); //支付成功时间
        String backType = params.getString("backType"); //通知类型

        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(mchOrderNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", mchOrderNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("payOrderId", payOrderId);
        signMap.put("mchId", mchId);
        signMap.put("appId", appId);
        signMap.put("productId", productId);
        signMap.put("mchOrderNo", mchOrderNo);
        signMap.put("amount", amount);
        signMap.put("income", income);
        signMap.put("status", status);
        signMap.put("channelOrderNo", channelOrderNo);
        signMap.put("channelAttach", channelAttach);
        signMap.put("param1", param1);
        signMap.put("param2", param2);
        signMap.put("paySuccTime", paySuccTime);
        signMap.put("backType", backType);

        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(amount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
