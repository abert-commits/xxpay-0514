package org.xxpay.pay.channel.mayi;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.TransOrder;
import org.xxpay.pay.channel.BaseTransNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class MayipayTransNotifyService extends BaseTransNotify {
    private static final MyLog _log = MyLog.getLog(MayipayTransNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理蚂蚁代付回调】";
        _log.info("====== 开始处理蚂蚁代付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        TransOrder transOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("retCode", request.getParameter("retCode"));
            params.put("retMsg", request.getParameter("retMsg"));
            params.put("merchantId", request.getParameter("merchantId"));
            params.put("orderId", request.getParameter("orderId"));
            params.put("outOrderId", request.getParameter("outOrderId"));
            params.put("amount", request.getParameter("amount"));
            params.put("payAmount", request.getParameter("payAmount"));
            params.put("totalFee", request.getParameter("totalFee"));
            params.put("status", request.getParameter("status"));
            params.put("transTime", request.getParameter("transTime"));
            params.put("sign", request.getParameter("sign"));
            _log.debug("代付回调============="+params.toJSONString());
            payContext.put("parameters", params);
            if(!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            transOrder = (TransOrder) payContext.get("transOrder");
            // 处理订单
            byte transStatus = transOrder.getStatus(); // 0-订单生成,1-转账中,2-转账成功,3-转账失败,4-业务处理完成
            if (transStatus != PayConstant.PAY_STATUS_SUCCESS && transStatus != PayConstant.PAY_STATUS_COMPLETE) {
                int updatePayOrderRows = rpcCommonService.rpcTransOrderService.updateStatus4Success(transOrder.getTransOrderId());
                if (updatePayOrderRows != 1) {
                    _log.error("{}更新代付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, transOrder.getTransOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    retObj.put(PayConstant.RESPONSE_RESULT, "处理订单失败");
                    return retObj;
                }
                _log.error("{}更新代付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, transOrder.getTransOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                transOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
            }
            // 业务系统后端通知
            baseNotify4MchTrans.doNotify(transOrder, true);
            _log.info("====== 完成处理蚂蚁代付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }


    /**
     * 验证蚂蚁支付通知参数
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String retCode = params.getString("retCode");           // 处理结果码
        String retMsg = params.getString("retMsg");            // 处理结果描述
        String memberid = params.getString("memberid");       // 商户编号
        String orderId = params.getString("orderId");		  // 平台订单号
        String outOrderId = params.getString("outOrderId");// 商户订单号
        String amount = params.getString("amount"); 		       // 扣款金额
        String payAmount = params.getString("payAmount"); 		     // 到账金额
        String totalFee = params.getString("totalFee"); 	     // 手续费
        String status = params.getString("status"); 		     // 订单状态
        String transTime = params.getString("transTime"); 		 // 系统订单完成时间
        String sign = params.getString("sign"); 		        //签名

        // 查询payOrder记录
        String transOrderId = outOrderId;
        TransOrder transOrder = rpcCommonService.rpcTransOrderService.findByTransOrderId(transOrderId);
        if (transOrder == null) {
            _log.error("Can't found transOrder form db. transOrderId={}, ", transOrderId);
            payContext.put("retMsg", "Can't found transOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getTransParam(transOrder));
        // 验证签名
        Map signMap = new HashMap<>();
        signMap.put("retCode", retCode);
        signMap.put("retMsg", retMsg);
        signMap.put("memberid", memberid);
        signMap.put("orderId", orderId);
        signMap.put("outOrderId", outOrderId);
        signMap.put("amount", amount);
        signMap.put("payAmount", payAmount);
        signMap.put("totalFee", totalFee);
        signMap.put("status", status);
        signMap.put("transTime", transTime);

        String signValue = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if(!sign.equals(signValue)) {
            _log.error("验证签名失败. transOrderId={}, ", transOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();
        long dbPayAmt = transOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},transOrderId={}", amount, transOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("transOrder", transOrder);
        return true;
    }

}
