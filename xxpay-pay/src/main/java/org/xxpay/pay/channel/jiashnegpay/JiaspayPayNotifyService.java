package org.xxpay.pay.channel.jiashnegpay;

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
public class JiaspayPayNotifyService extends BasePayNotify {
    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_JIASHENG;
    }

    private static final MyLog _log = MyLog.getLog(JiaspayPaymentService.class);

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理嘉盛支付回调】";
        _log.info("====== 开始处理嘉盛支付回调通知 ======");
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
            params.put("merchantnum", jsonParam.getString("merchantnum"));
            params.put("turemoney", jsonParam.getString("turemoney"));
            params.put("qrcode_type", jsonParam.getString("qrcode_type"));
            params.put("merchanturl", jsonParam.getString("merchanturl"));
            params.put("qudao_type", jsonParam.getString("qudao_type"));
            params.put("ptorder", jsonParam.getString("ptorder"));
            params.put("sign", jsonParam.getString("sign"));
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
            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理嘉盛支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证嘉盛支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String amount = params.getString("turemoney"); // 订单金额
        BigDecimal b = new BigDecimal(params.getString("turemoney"));
        amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String ptorder = params.getString("ptorder"); // 平台流水号
        String outOrderNo = params.getString("merchantnum"); // 商户订单号
        String merchanturl = params.getString("merchanturl"); //订单请求时间
        String qrcodeType = params.getString("qrcode_type"); //二维码类型
        String qudaoType = params.getString("qudao_type"); //二维码类型
        String sign = params.getString("sign");  //签名
        // 查询payOrder记录
        String payOrderId = outOrderNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

        // md5 加密
        String md5 = PayDigestUtil.md5(outOrderNo+
                amount + qrcodeType  + merchanturl + ptorder + qudaoType + channelPayConfig.getmD5Key(), "utf-8").toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }


}
