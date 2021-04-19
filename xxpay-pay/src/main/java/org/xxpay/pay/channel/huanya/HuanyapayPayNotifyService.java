package org.xxpay.pay.channel.huanya;

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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class HuanyapayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HuanyapayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUANYA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理环亚支付回调】";
        _log.info("====== 开始处理环亚支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("p_data", jsonParam.getString("p_data"));
            params.put("orderNo", jsonParam.getString("orderNo"));
            params.put("createTime", jsonParam.getString("createTime"));
            params.put("payStatus", jsonParam.getString("payStatus"));
            params.put("amount", jsonParam.getString("amount"));
            params.put("price", jsonParam.getString("price"));
            params.put("tradeType", jsonParam.getString("tradeType"));
            params.put("sign", jsonParam.getString("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");

            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
            if ("1".equals(params.get("payStatus"))) {
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
            _log.info("====== 完成处理环亚支付回调通知 ======");
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
        String p_data = params.getString("p_data");  //预留参数
        String orderNo = params.getString("orderNo"); // 商户唯一订单号
        String createTime = params.getString("createTime"); // 订单创建订单
        String payStatus = params.getString("payStatus");   //交易状态
        String amount = params.getString("amount"); // 到账金额
        String price = params.getString("price"); //订单金额
        String tradeType = params.getString("tradeType"); //支付通道


        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        String payOrderId = orderNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        if (!StringUtil.isBlank(p_data)) {
            signMap.put("p_data", p_data);

        }
        if (!StringUtil.isBlank(orderNo)) {

            signMap.put("orderNo", orderNo);
        }
        if (!StringUtil.isBlank(createTime)) {

            signMap.put("createTime", createTime);
        }
        if (!StringUtil.isBlank(payStatus)) {

            signMap.put("payStatus", payStatus);
        }
        if (!StringUtil.isBlank(amount)) {

            signMap.put("amount", amount);
        }
        if (!StringUtil.isBlank(price)) {

            signMap.put("price", price);
        }
        if (!StringUtil.isBlank(tradeType)) {

            signMap.put("tradeType", tradeType);
        }

        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(price)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
