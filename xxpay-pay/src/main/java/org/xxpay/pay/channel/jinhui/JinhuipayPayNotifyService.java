package org.xxpay.pay.channel.jinhui;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
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

import static org.xxpay.core.common.util.PayDigestUtil.md5;

@Service
public class JinhuipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(JinhuipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理金汇支付回调】";
        _log.info("====== 开始处理金汇支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("result_code", request.getParameter("result_code"));
            params.put("mch_id", request.getParameter("mch_id"));
            params.put("trade_type", request.getParameter("trade_type"));
            params.put("nonce", request.getParameter("nonce"));
            params.put("timestamp", request.getParameter("timestamp"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("total_fee", request.getParameter("total_fee"));
            params.put("trade_no", request.getParameter("trade_no"));
            params.put("platform_trade_no", request.getParameter("platform_trade_no"));
            params.put("pay_time", request.getParameter("pay_time"));
            params.put("real_price", request.getParameter("real_price"));

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
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款
            if ("SUCCESS".equals(params.get("result_code"))) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId(), params.getString("platform_trade_no"));
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
            _log.info("====== 完成处理金汇支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证金汇支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String result_code = params.getString("result_code");  //业务结果
        String mch_id = params.getString("mch_id"); // 商户号
        String trade_type = params.getString("trade_type"); // 交易类型
        String nonce = params.getString("nonce");   //随机字符串
        String timestamp = params.getString("timestamp"); // 时间戳
        String out_trade_no = params.getString("out_trade_no"); //商户订单号
        long total_fee = params.getLong("total_fee"); //总金额
        String trade_no = params.getString("trade_no"); //交易流水号
        String platform_trade_no = params.getString("platform_trade_no"); //平台交易单号
        String pay_time = params.getString("pay_time"); //支付时间


        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        String payOrderId = out_trade_no;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("result_code", result_code);
        signMap.put("mch_id", mch_id);
        signMap.put("trade_type", trade_type);
        signMap.put("nonce", nonce);
        signMap.put("timestamp", timestamp);
        signMap.put("out_trade_no", out_trade_no);
        signMap.put("total_fee", String.valueOf(total_fee));
        signMap.put("trade_no", trade_no);
        signMap.put("platform_trade_no", platform_trade_no);
        signMap.put("pay_time", pay_time);
        signMap.put("real_price", params.getString("real_price"));
        /*// 第一步拼接字符串：
        String stringMap = XXPayUtil.mapToString(signMap).replace(">", "");
*/
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        // RSA2 验签
        ///boolean md5Sign = PayDigestUtil.verify(md5, PayDigestUtil.getPublicKey(channelPayConfig.getRsaPublicKey()), sign, "utf8");

        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != total_fee) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total_fee, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
