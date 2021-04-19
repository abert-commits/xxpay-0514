package org.xxpay.pay.channel.haha;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
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
public class HahaPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HahaPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HAHA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理哈哈支付回调】";
        _log.info("====== 开始处理哈哈支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("callbacks", request.getParameter("callbacks"));
            params.put("type", request.getParameter("type"));
            params.put("total", request.getParameter("total"));
            params.put("api_order_sn", request.getParameter("api_order_sn"));
            params.put("order_sn", request.getParameter("order_sn"));
            params.put("sign", request.getParameter("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            int updatePayOrderRows=0;
            if ("CODE_SUCCESS".equals(params.get("callbacks"))) {
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
            if (updatePayOrderRows>0)
            {
                baseNotify4MchPay.doNotify(payOrder, true);
            }

            _log.info("====== 完成处理哈哈支付回调通知 ======");
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
        String callbacks	 = params.getString("callbacks");  //状态值 ： CODE_SUCCESS 成功， CODE_FAILURE 失败
        String type = params.getString("type"); // 支付方式
        String total = params.getString("total"); // 支付总额
        String api_order_sn = params.getString("api_order_sn");   //客户端订单编号
        String order_sn = params.getString("order_sn"); //拼多多订单编号
        String sign = params.getString("sign"); //签名



        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(api_order_sn);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", api_order_sn);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, Object> signMap = new TreeMap<String, Object>();
        signMap.put("callbacks", callbacks	);
        signMap.put("type", type);
        signMap.put("total", total);
        signMap.put("api_order_sn", api_order_sn);
        signMap.put("order_sn", order_sn);
        // md5 加密
        String md5 = HahaPaymentService.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", api_order_sn);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(total))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total, api_order_sn);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
