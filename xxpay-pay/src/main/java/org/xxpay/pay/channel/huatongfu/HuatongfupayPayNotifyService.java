package org.xxpay.pay.channel.huatongfu;

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
public class HuatongfupayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(HuatongfupayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理华通付支付回调】";
        _log.info("====== 开始处理华通付支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RESULT_PARAM_SIGN;
        try {
            JSONObject params = new JSONObject();
            params.put("account_name", request.getParameter("account_name"));
            params.put("pay_time", request.getParameter("pay_time"));
            params.put("status", request.getParameter("status"));
            params.put("amount", request.getParameter("amount"));
            params.put("out_trade_no", request.getParameter("out_trade_no"));
            params.put("amount", request.getParameter("amount"));
            params.put("trade_no", request.getParameter("trade_no"));
            params.put("rand", request.getParameter("rand"));
            params.put("origin_amount", request.getParameter("origin_amount"));
            params.put("ext", request.getParameter("ext"));
            params.put("is_change", request.getParameter("is_change"));
            params.put("callback_time", request.getParameter("callback_time"));

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
            if ("success".equals(params.get("status"))) {
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
            _log.info("====== 完成处理华通付支付回调通知 ======");
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
        String account_name = params.getString("account_name");  //商户号
        String out_trade_no = params.getString("out_trade_no"); //外部订单号
        String amount = params.getString("amount"); //支付金额
        String rand = params.getString("rand"); //随机金额

        String sign = params.getString("sign");  //签名

        // 查询payOrder记录
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(out_trade_no);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", out_trade_no);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

        //第一步：将金额转两位小数，即1.00，同时拼接上外部订单号，得到的结果为： MD5(1.0020191298678976567)	=	5fe80d9f797f3b5ffa543d7a6802a16e
        String md51 = PayDigestUtil.md5(amount + payOrder.getPayOrderId(), "UTF-8");

        //第二步：将得到的字符串拼接上小写的MD5秘钥（商户后台获取）即： MD5(d93e4522687525438068cff627333e6d5fe80d9f797f3b5ffa543d7a6802a16e)
        String md52 = PayDigestUtil.md5(channelPayConfig.getmD5Key().toLowerCase() + md51, "UTF-8");

        if (!md52.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrder.getPayOrderId());
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额
        long dbPayAmt = payOrder.getAmount().longValue();
        long outPayAmt = new BigDecimal(amount).add(new BigDecimal(rand)).multiply(new BigDecimal(100)).longValue();//乘以100
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", amount, payOrder.getPayOrderId());
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
