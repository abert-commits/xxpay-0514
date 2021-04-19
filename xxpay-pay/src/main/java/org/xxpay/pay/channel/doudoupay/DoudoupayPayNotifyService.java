package org.xxpay.pay.channel.doudoupay;

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
public class DoudoupayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(DoudoupayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DOUDOU;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理豆豆支付回调】";
        _log.info("====== 开始处理豆豆支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        try {
            JSONObject params = new JSONObject();
            params.put("merchant_id", jsonParam.getString("merchant_id"));
            params.put("nonce_str", jsonParam.getString("nonce_str"));
            params.put("version", jsonParam.getString("version"));
            params.put("response_time",jsonParam.getString("response_time"));
            params.put("sign_type",jsonParam.getString("sign_type"));
            params.put("sign",jsonParam.getString("sign"));
            params.put("data",jsonParam.getString("data"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            int updatePayOrderRows = 0;
            System.out.println("回调订单号++++《《《《》》》》》"+payOrder.getPayOrderId());
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
            if (updatePayOrderRows == 1) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
                _log.info("====== 完成处理豆豆支付回调通知 ======");
            }
            respString = PayConstant.RETURN_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证豆豆支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String version = params.getString("version");// 版本号
        String memberid = params.getString("merchant_id");  //商户号
        String response_time = params.getString("response_time"); //交易时间
        String nonceStr = params.getString("nonce_str"); //随机字符串
        String data = params.getString("data"); // 数据类型data
        String signType = params.getString("sign_type");//签名类型
        String sign = params.getString("sign");  //签名
        JSONObject jsonObject = JSONObject.parseObject(data);
        String outOrderNo = jsonObject.getString("order_no");
        String trade_no = jsonObject.getString("trade_no");
        String amount = jsonObject.getString("amount");
        BigDecimal b = new BigDecimal(jsonObject.getString("amount"));
        amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        String pay_amount = jsonObject.getString("pay_amount");
        String trade_code = jsonObject.getString("trade_code");
        String status = jsonObject.getString("status");
        String complete_time = jsonObject.getString("complete_time");
        // 查询payOrder记录
        String payOrderId = outOrderNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("version", version);
        signMap.put("merchant_id", memberid);
        signMap.put("nonce_str", nonceStr);
        signMap.put("order_no", outOrderNo);
        signMap.put("amount",amount);
        signMap.put("pay_amount", pay_amount);
        signMap.put("trade_code", trade_code);
        signMap.put("trade_no",trade_no);
        signMap.put("status", status);
        signMap.put("complete_time", complete_time);
        signMap.put("response_time",response_time);
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(pay_amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
