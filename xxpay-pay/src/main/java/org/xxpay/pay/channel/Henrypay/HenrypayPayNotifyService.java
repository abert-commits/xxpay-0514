package org.xxpay.pay.channel.Henrypay;

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
public class HenrypayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HenrypayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUANYA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理Henry支付回调】";
        _log.info("====== 开始处理Henry支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);

        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("no", jsonParam.getString("no"));
            params.put("outTradeNo", jsonParam.getString("outTradeNo"));
            params.put("merchantNo", jsonParam.getString("merchantNo"));
            params.put("productId", jsonParam.getString("productId"));
            params.put("money", jsonParam.getString("money"));
            params.put("body", jsonParam.getString("body"));
            params.put("detail", jsonParam.getString("detail"));
            params.put("tradeType", jsonParam.getString("tradeType"));
            params.put("date", jsonParam.getString("date"));
            params.put("nonce", jsonParam.getString("nonce"));
            params.put("timestamp", jsonParam.getString("timestamp"));
            params.put("sign", jsonParam.getString("sign"));
            params.put("success", jsonParam.getString("success"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付装态（付款成功：true,付款失败：false,未付款:null）
            if (jsonParam.getBoolean("success")) {
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
            _log.info("====== 完成处理henry支付回调通知 ======");
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
        String no = params.getString("no");  //系统订单号
        String outTradeNo = params.getString("outTradeNo"); //商户订单号
        String merchantNo = params.getString("merchantNo"); // 商户号
        String productId = params.getString("productId");   //商品ID
        long money = params.getLong("money"); //支付金额(分)
        String body = params.getString("body"); //订单内容
        String detail = params.getString("detail"); //订单详情
        String tradeType = params.getString("tradeType"); //结算类型(如T0、T1)
        String date = params.getString("date"); //支付时间
        String nonce = params.getString("nonce"); //随机字符
        String timestamp = params.getString("timestamp"); //时间戳
        String sign = params.getString("sign");  //签名
        String success = params.getString("success"); //支付装态（付款成功：true,付款失败：false,未付款:null）

        // 查询payOrder记录
        String payOrderId = outTradeNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("no", no);
        signMap.put("merchantNo", merchantNo);
        signMap.put("nonce", nonce);
        signMap.put("timestamp", timestamp);
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != money) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
