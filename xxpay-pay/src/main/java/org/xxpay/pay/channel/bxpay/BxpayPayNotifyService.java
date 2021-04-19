package org.xxpay.pay.channel.bxpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BxpayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(BxpayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_WAWAJIAOPAY;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理BX支付回调】";
        _log.info("====== 开始处理BX支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_VALUE_FAIL;
        try {
            JSONObject params = new JSONObject();
            params.put("mno", request.getParameter("mno"));
            params.put("orderno", request.getParameter("orderno"));
            params.put("content", request.getParameter("content"));
            _log.info("BX支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 1-支付成功
//            if ("0".equals(params.get("opstate"))) {
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
//            }
            if (updatePayOrderRows > 0) {
                // 业务系统后端通知
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理BX支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证BX支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String mno = params.getString("mno");  //商户号
        String orderno = params.getString("orderno"); // 订单号
        String content = params.getString("content"); // 文本


        // 查询payOrder记录
        String payOrderId = orderno;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

        String rsaprivateKey = channelPayConfig.getRsaprivateKey();
        String str = rsaprivateKey.substring(rsaprivateKey.length()-16, rsaprivateKey.length());
        String contents = AESUtils.decryptBase64(content, str);

//        String contexts = Base64Utils.encode(RSASHA256withRSAUtils.decryptByPrivateKey(contents.getBytes(),channelPayConfig.getRsaprivateKey()));
        JSONObject jsonObject = JSONObject.parseObject(contents);
        String money = jsonObject.getString("amount");
        String status = jsonObject.getString("status");
        String paytime = jsonObject.getString("paytime");
        String sign = jsonObject.getString("sign");

        Map jsonObjects = new TreeMap();
        jsonObjects.put("amount",money);
        jsonObjects.put("mno",mno);
        jsonObjects.put("orderno",orderno);
        jsonObjects.put("paytime",paytime);
        jsonObjects.put("status",status);
        jsonObjects.remove("sign");
        String s = XXPayUtil.mapToStringByObj(jsonObjects).replace(">", "");
        System.out.println("回调签名参数》》》》《《《》》》" + s);
        String md5  = Base64Utils.encode(RSASHA256withRSAUtils.encryptByPrivateKey(s.getBytes(),rsaprivateKey));
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }

        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(money)) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }

        payContext.put("payOrder", payOrder);
        return true;
    }
}
