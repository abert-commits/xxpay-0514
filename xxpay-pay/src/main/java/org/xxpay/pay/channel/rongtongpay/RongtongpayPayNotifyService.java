package org.xxpay.pay.channel.rongtongpay;

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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.xxpay.core.common.util.PayDigestUtil.*;

@Service
public class RongtongpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(RongtongpayPayNotifyService.class);

    private static String encodingCharset = "UTF-8";
    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DOUDOU;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理融通支付回调】";
        _log.info("====== 开始处理融通支付回调通知 ======");
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
           /* params.put("callbacks", request.getParameter("callbacks"));
            params.put("total", request.getParameter("total"));
            params.put("paytime", request.getParameter("paytime"));
            params.put("order_sn", request.getParameter("order_sn"));*/

            params.put("merchant_id", jsonParam.getString("merchant_id"));
            params.put("request_time", jsonParam.getString("request_time"));
            params.put("pay_time", jsonParam.getString("pay_time"));
            params.put("status", jsonParam.getString("status"));
            params.put("order_amount", jsonParam.getString("order_amount"));
            params.put("pay_amount", jsonParam.getString("pay_amount"));
            params.put("out_trade_no", jsonParam.getString("out_trade_no"));
            params.put("trade_no", jsonParam.getString("trade_no"));

            params.put("fees", jsonParam.getString("fees"));
            params.put("pay_type", jsonParam.getString("pay_type"));
            params.put("nonce_str", jsonParam.getString("nonce_str"));
            params.put("remarks", jsonParam.getString("remarks"));

            params.put("sign",jsonParam.getString("sign"));
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
            int updatePayOrderRows = 0;
            System.out.println("回调订单号++++《《《《》》》》》"+payOrder.getPayOrderId());
            // 处理订单
            if("success".equals(params.getString("status"))) {
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
            }
            if (updatePayOrderRows == 1) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
                _log.info("====== 完成处理融通支付回调通知 ======");
            }
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }
    public static String getSign(Map<String, Object> map, String key) throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(entry.getKey() +  getSortJson((JSONObject) entry.getValue()) );
                } else {
                    list.add(entry.getKey() + entry.getValue());
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result =key+result+ key;
        _log.info("Sign Before MD5:" + result);
        result = md5(result, encodingCharset).toLowerCase();
        _log.info("Sign Result:" + result);
        return result;
    }
    /**
     * 验证融通支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功

        String pay_amount = params.getString("pay_amount");
        String out_trade_no = params.getString("out_trade_no");
        String sign = params.getString("sign");
        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(out_trade_no);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        params.remove("sign");

        // 验证签名
        String rsign =getSignNotKey(params,channelPayConfig.getmD5Key()).toUpperCase();
        if (!rsign.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(pay_amount).multiply(new BigDecimal(100)).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", pay_amount, out_trade_no);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
