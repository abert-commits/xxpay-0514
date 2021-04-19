package org.xxpay.pay.channel.mayi;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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

@Service
public class MayipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(MayipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理蚂蚁支付回调】";
        _log.info("====== 开始处理蚂蚁支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("returncode", request.getParameter("returncode"));
            params.put("memberid", request.getParameter("memberid"));
            params.put("orderid", request.getParameter("orderid"));
            params.put("amount", request.getParameter("amount"));
            params.put("money", request.getParameter("money"));
            params.put("channelsn", request.getParameter("channelsn"));
            params.put("datetime", request.getParameter("datetime"));
            params.put("sign", request.getParameter("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows=0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
            if ("00".equals(params.get("returncode"))) {
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

            if (updatePayOrderRows>0)
            {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理蚂蚁支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证蚂蚁支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String returncode = params.getString("returncode");            // 处理结果码
        String memberid = params.getString("memberid");                // 商户编号
        String orderid = params.getString("orderid");                    // 商户订单号
        String amount = params.getString("amount");                    // 订单金额
        String money = params.getString("money");                    // 实际支付金额
        String channelsn = params.getString("channelsn");                // 平台订单号
        String datetime = params.getString("datetime");                //系统订单完成时间
        String sign = params.getString("sign");                //签名

        // 查询payOrder记录
        String payOrderId = orderid;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        Map signMap = new HashMap<>();
        signMap.put("returncode", returncode);
        signMap.put("memberid", memberid);
        signMap.put("orderid", orderid);
        signMap.put("amount", amount);
        signMap.put("money", money);
        signMap.put("channelsn", channelsn);
        signMap.put("datetime", datetime);
        String signValue="";
        if (!StringUtil.isBlank(channelPayConfig.getRsapassWord()))
        {
            signValue = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        }else {
            signValue = PayDigestUtil.getSignPhP(signMap, channelPayConfig.getmD5Key());
        }

        if (!sign.equals(signValue)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(money).multiply(new BigDecimal(100)).longValue();
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
