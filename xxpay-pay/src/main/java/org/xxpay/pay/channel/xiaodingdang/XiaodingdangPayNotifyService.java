package org.xxpay.pay.channel.xiaodingdang;

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

@Service
public class XiaodingdangPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(XiaodingdangPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YUANZHIFU;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理小叮当回调】";
        _log.info("====== 开始处理小叮当回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);

        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("merNo", jsonParam.getString("merNo"));
            params.put("orderNo", jsonParam.getString("orderNo"));
            params.put("merOrderNo", jsonParam.getString("merOrderNo"));
            params.put("notifyType", jsonParam.getString("notifyType"));
            params.put("payType", jsonParam.getString("payType"));
            params.put("tradeAmt", jsonParam.getString("tradeAmt"));
            params.put("actualPayAmt", jsonParam.getString("actualPayAmt"));
            params.put("payTime", jsonParam.getString("payTime"));
            params.put("respCode", jsonParam.getString("respCode"));
            params.put("respDesc",jsonParam.getString("respDesc"));
            params.put("signature",jsonParam.getString("signature"));

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
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if ("交易成功".equals(params.get("respDesc"))) {
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

            if (updatePayOrderRows == 1) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理小叮当" +
                    "回调通知 ======");
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
        String tradeAmt = params.getString("tradeAmt"); //支付金额
        String merOrderNo = params.getString("merOrderNo"); //商户订单号
        String sign = params.getString("signature");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(merOrderNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", merOrderNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        params.remove("signature");

        // md5 加密
        String md5 = PayDigestUtil.getSignNotKey(params, channelPayConfig.getmD5Key()).toLowerCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", merOrderNo);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(tradeAmt).multiply(new BigDecimal(100)).longValue();

        if (dbPayAmt != Long.valueOf(resAmount)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", tradeAmt, merOrderNo);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }
    
}
