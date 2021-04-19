package org.xxpay.pay.channel.dabaopay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
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
public class DabaopayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(DabaopayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DABAO;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理大宝宝支付回调】";
        _log.info("====== 开始处理大宝宝支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        System.out.println("大宝宝下单回调参数??>>><<<<<++++++?>>>>" + request.getParameterMap());
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("orderSn", request.getParameter("orderSn"));
            params.put("outTradeNo", request.getParameter("outTradeNo"));
            params.put("userAgent", request.getParameter("userAgent"));
            params.put("appId", request.getParameter("appId"));
            params.put("money", request.getParameter("money"));
            params.put("respMsg", request.getParameter("respMsg"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("status", request.getParameter("status"));
            params.put("charset", request.getParameter("charset"));
            params.put("sign", request.getParameter("sign"));

            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");

//            if (!CheckCallIP(request, payOrder.getPassageAccountId(), payOrder)) {
//                respString = "回调IP非白名单";
//                retObj.put(PayConstant.RESPONSE_RESULT, respString);
//                return  retObj;
//            }

            int updatePayOrderRows = 0;
            System.out.println("回调订单号++++《《《《》》》》》"+payOrder.getPayOrderId());
            // 处理订单

            if (params.getString("status").equals("2"))
            {
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
                }
            }

            _log.info("====== 完成处理大宝宝支付回调通知 ======");
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证大宝宝支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String orderSn = params.getString("orderSn");  //状态值 ： CODE_SUCCESS 成功， CODE_FAILURE 失败
        String outTradeNo = params.getString("outTradeNo"); // 支付方式
        String userAgent = params.getString("userAgent"); // 支付总额
        String appId = params.getString("appId");   //商户号
        String money = params.getString("money"); //金额
        String respMsg = params.getString("respMsg");

        String respCode = params.getString("respCode"); //返回码
        String status = params.getString("status"); //拼多多订单编号
        String sign = params.getString("sign"); //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(outTradeNo);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", outTradeNo);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap signMap = new TreeMap();
        signMap.put("orderSn", orderSn);
        signMap.put("outTradeNo", outTradeNo);
        signMap.put("userAgent",userAgent);
        signMap.put("appId", appId);
        signMap.put("money", money);
        signMap.put("respMsg", respMsg);
        signMap.put("respCode", respCode);
        signMap.put("status", status);
        signMap.put("charset",params.getString("charset"));
        // md5 加密
        String requestParam = XXPayUtil.mapToString(signMap).replace(">", "");
        boolean verify = RSASHA256withRSAUtils.verify(requestParam.getBytes(), channelPayConfig.getRsaPublicKey(),sign);
        if (!verify) {
            _log.error("验证签名失败. payOrderId={}, ", outTradeNo);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(money))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, outTradeNo);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
