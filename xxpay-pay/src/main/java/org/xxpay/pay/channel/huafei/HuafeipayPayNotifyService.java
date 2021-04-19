package org.xxpay.pay.channel.huafei;

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
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class HuafeipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HuafeipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUANYA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理华东支付回调】";
        _log.info("====== 开始处理华东支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("merchant_code", request.getParameter("merchant_code"));
            params.put("service_type", request.getParameter("service_type"));
            params.put("coin_type", request.getParameter("coin_type"));
            params.put("status", request.getParameter("status"));
            params.put("order_no", request.getParameter("order_no"));
            params.put("order_time", request.getParameter("order_time"));
            params.put("amount", request.getParameter("amount"));
            params.put("host_no", request.getParameter("host_no"));
            params.put("host_time", request.getParameter("host_time"));
            params.put("product_name", request.getParameter("product_name"));


            params.put("sign", request.getParameter("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            if (!CheckCallIP(request, payOrder.getPassageId(), payOrder)) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return  retObj;
            }
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 00 成功（必须判断状态） 99 交易关闭 FF 支付失败
            if ("00".equals(params.get("status"))) {
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
            _log.info("====== 完成处理华东支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK;
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
        String merchant_code = params.getString("merchant_code");  //商家号
        String service_type = params.getString("service_type"); // 交易类型
        String coin_type = params.getString("coin_type"); // 币种
        String status = params.getString("status");   //处理码
        String order_no = params.getString("order_no"); // 商家订单号
        String order_time = params.getString("order_time"); //商家订单时间
        String amount = params.getString("amount"); //商家订单金额
        String host_no = params.getString("host_no"); //平台订单号
        String host_time = params.getString("host_time"); //平台订单时间
        String product_name = params.getString("product_name"); //商品名称


        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        String payOrderId = order_no;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("merchant_code", merchant_code);
        signMap.put("service_type", service_type);
        signMap.put("coin_type", coin_type);
        signMap.put("status", status);
        signMap.put("order_no", order_no);
        signMap.put("order_time", order_time);
        signMap.put("amount", amount);
        signMap.put("host_no", host_no);
        signMap.put("host_time", host_time);
        signMap.put("product_name", product_name);


        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long outPayAmt = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();

        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(outPayAmt)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
