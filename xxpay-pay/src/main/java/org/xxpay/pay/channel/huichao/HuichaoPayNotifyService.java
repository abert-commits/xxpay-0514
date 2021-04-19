package org.xxpay.pay.channel.huichao;

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
import java.util.TreeMap;

@Service
public class HuichaoPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HuichaoPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HUICHAO;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理汇潮支付回调】";
        _log.info("====== 开始处理汇潮支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("money", request.getParameter("money"));
            params.put("pay_money", request.getParameter("pay_money"));
            params.put("out_trade_sn", request.getParameter("out_trade_sn"));
            params.put("order_sn", request.getParameter("order_sn"));
            params.put("pay_order_sn", request.getParameter("pay_order_sn"));
            params.put("attch", request.getParameter("attch"));
            params.put("return_url", request.getParameter("return_url"));
            params.put("notify_url", request.getParameter("notify_url"));
            params.put("sign", request.getParameter("sign"));
            _log.info("支付回调=============" + params);

            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            int updatePayOrderRows = 0;
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
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

            _log.info("====== 完成处理汇潮支付回调通知 ======");
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
        String money = params.getString("money");  //充值金额
        String pay_money = params.getString("pay_money"); // 成功支付金额
        String out_trade_sn = params.getString("out_trade_sn"); // 您的支付订单号
        String order_sn = params.getString("order_sn");   //平台订单号
        String pay_order_sn = params.getString("pay_order_sn"); //支付交易流水号
        String attch = params.getString("attch"); //附加信息
        String return_url = params.getString("return_url"); //签名认证字符串
        String notify_url = params.getString("notify_url"); //同步回调地址


        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(out_trade_sn);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", out_trade_sn);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("money", money);
        signMap.put("out_trade_sn", out_trade_sn);
        signMap.put("return_url", return_url);
        signMap.put("notify_url", notify_url);
        signMap.put("app_id", channelPayConfig.getMchId());

        String signMsg = XXPayUtil.mapToString(signMap).replace(">", "");
        String retSign = PayDigestUtil.md5((signMsg + "&token=" + channelPayConfig.getmD5Key()).toLowerCase(), "utf-8");
        retSign=PayDigestUtil.md5(channelPayConfig.getmD5Key()+retSign,"utf-8");
        // md5 加密
        if (!sign.equals(retSign)) {
            _log.error("验证签名失败. payOrderId={}, ", out_trade_sn);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != Long.valueOf(AmountUtil.convertDollar2Cent(pay_money))) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", pay_money, out_trade_sn);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
