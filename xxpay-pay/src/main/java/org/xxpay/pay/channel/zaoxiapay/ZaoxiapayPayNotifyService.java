package org.xxpay.pay.channel.zaoxiapay;

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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.xxpay.core.common.util.PayDigestUtil.toHex;

@Service
public class ZaoxiapayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(ZaoxiapayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ZAOXIA;
    }

    private static String encodingCharset = "UTF-8";

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理造虾支付回调】";
        _log.info("====== 开始处理造虾支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        System.out.println("造虾回调处理+++___>><<<<<<>>>>>" + request.getParameterMap());
        //获取到JSONObject

        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("merchant_order_no", request.getParameter("merchant_order_no"));
            params.put("merchant_no", request.getParameter("merchant_no"));
            params.put("order_no", request.getParameter("order_no"));
            params.put("product_id", request.getParameter("product_id"));
            params.put("money", request.getParameter("money"));
            params.put("body", request.getParameter("body"));
            params.put("date", request.getParameter("date"));
            params.put("nonce", request.getParameter("nonce"));
            params.put("timestamp", request.getParameter("timestamp"));
            params.put("sign", request.getParameter("sign"));
            params.put("success", request.getParameter("success"));
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
            _log.info("====== 完成处理造虾支付回调通知 ======");
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
        String outTradeNo = params.getString("merchant_order_no"); //商户订单号
        String order_no = params.getString("order_no"); // 上游订单号
        String merchantNo = params.getString("merchant_no"); // 商户号
        String productId = params.getString("product_id");   //商品ID
        long moneys = params.getLong("money"); //支付金额(分)
        String money = String.valueOf(moneys);
        String body = params.getString("body"); //订单内容
        String date = params.getString("date"); //支付时间
        String nonce = params.getString("nonce"); //随机字符
        String timestamp = params.getString("timestamp"); //时间戳
        String sign = params.getString("sign");  //签名
        String success = params.getString("success"); //支付装态（付款成功：true,付款失败：false,未付款:null）

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
        signMap.put("merchant_order_no", outTradeNo);
        signMap.put("order_no", order_no);
        signMap.put("product_id", productId);
        signMap.put("money", money);
        signMap.put("body", body);
        signMap.put("date", date);
        signMap.put("nonce", nonce);
        signMap.put("timestamp", timestamp);
        signMap.put("success", success);
        // md5 加密

        String md5 = PayDigestUtil.md5(XXPayUtil.mapToString(signMap)+"&" + channelPayConfig.getmD5Key(), "UTF-8").toUpperCase();
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(money).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
