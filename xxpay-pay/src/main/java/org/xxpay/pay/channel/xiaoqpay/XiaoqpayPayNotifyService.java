package org.xxpay.pay.channel.xiaoqpay;

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
public class XiaoqpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(XiaoqpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_XIAOQIAN;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理小钱袋支付回调】";
        _log.info("====== 开始处理小钱袋支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("appid", request.getParameter("appid"));
            params.put("total_amount", request.getParameter("total_amount"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("trade_no", request.getParameter("trade_no"));
            params.put("respDesc", request.getParameter("respDesc"));
            params.put("code", request.getParameter("code"));
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
            if ("200".equals(params.get("code"))){
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

            _log.info("====== 完成处理小钱袋支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
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
        String appid = params.getString("appid"); // 商户ID
        String out_trade_no = params.getString("out_trade_no"); //商户订单号
        String total_amount = params.getString("total_amount"); //支付金额
        String trade_no = params.getString("trade_no"); //平台订单号
        String respDesc = params.getString("respDesc"); //订单详细
        String code = params.getString("code"); //状态
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
        signMap.put("appid", appid);
        signMap.put("total_amount", total_amount);
        signMap.put("out_trade_no", out_trade_no);
        signMap.put("trade_no", trade_no);
        signMap.put("respDesc", respDesc);
        signMap.put("code", code);

        // md5 加密
        String md5 = PayDigestUtil.getSignCustomizeKey(signMap, channelPayConfig.getmD5Key(),"appsecret");
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(total_amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total_amount, out_trade_no);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
