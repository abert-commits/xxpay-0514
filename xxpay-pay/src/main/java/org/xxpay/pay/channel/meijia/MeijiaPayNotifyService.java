package org.xxpay.pay.channel.meijia;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
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

@Service
public class MeijiaPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(MeijiaPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理美家支付回调】";
        _log.info("====== 开始处理叫娃娃支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder = null;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {

            String api_order_sn=request.getParameter("api_order_sn");
            payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(api_order_sn);
            String order_status=request.getParameter("order_status");
            String token=request.getParameter("token");

            _log.info("美家支付回调=============" + api_order_sn);
            if (!payOrder.getChannelOrderNo().equals(token)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            if (!CheckCallIP(request, payOrder.getPassageId(), payOrder)) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return retObj;
            }

            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            if (order_status.equals("success")) {
                if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId(),token);
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

            _log.info("====== 完成处理美家支付回调通知 ======");
            respString = PayConstant.RETURN_VALUE_OK;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        String orderid = params.getString("orderid"); // 商户订单号
        String money = params.getString("money");   //订单金额
        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(orderid);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", orderid);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        params.remove("sign");
        params.remove("attach");
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        SortedMap map = XXPayUtil.JSONObjectToSortedMap(params);
        String strSign = XXPayUtil.mapToString(map) + channelPayConfig.getmD5Key();
        String md5 = PayDigestUtil.md5(strSign, "utf-8");
        // md5 加密
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", orderid);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long outPayAmt= Long.parseLong(AmountUtil.convertDollar2Cent(money));
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", money, orderid);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
