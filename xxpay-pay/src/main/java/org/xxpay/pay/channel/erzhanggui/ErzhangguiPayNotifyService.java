package org.xxpay.pay.channel.erzhanggui;

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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ErzhangguiPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(ErzhangguiPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ERZHANGGUI;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理二掌柜支付回调】";
        _log.info("====== 开始处理二掌柜支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("platform_order_id", request.getParameter("platform_order_id"));
            params.put("order_id", request.getParameter("order_id"));
            params.put("post_amount", request.getParameter("post_amount"));
            params.put("real_amount", request.getParameter("real_amount"));
            params.put("callback_info", request.getParameter("callback_info"));
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

            _log.info("====== 完成处理大发支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK_1;
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
        String platform_order_id = params.getString("platform_order_id");  //平台生成的订单ID号
        String order_id = params.getString("order_id"); // 您的自定义订单号
        String post_amount = params.getString("post_amount"); // 提交金额
        String real_amount = params.getString("real_amount");   //支付金额
        String callback_info = params.getString("callback_info"); //商户自定义参数
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(order_id);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", order_id);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("platform_order_id", platform_order_id);
        signMap.put("order_id", order_id);
        signMap.put("post_amount", post_amount);
        signMap.put("real_amount", real_amount);
        signMap.put("callback_info", callback_info);
        String string = XXPayUtil.mapToString(signMap);
        RsaErzhangui rsaErzhangui = new RsaErzhangui();
        boolean b = rsaErzhangui.verify(string, sign, channelPayConfig.getRsaPublicKey());
        if (!b) {
            _log.error("验证签名失败. payOrderId={}, ", order_id);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(post_amount))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", post_amount, order_id);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}