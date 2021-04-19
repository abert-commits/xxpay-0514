package org.xxpay.pay.channel.zhuroupay;

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
public class ZhuroupayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(ZhuroupayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DOUDOU;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理猪肉支付回调】";
        _log.info("====== 开始处理猪肉支付回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        System.out.println("接收Zhuroupay回调参数+++——————+++++++》》》》《《《《《" + request.getParameterMap());
        //获取到JSONObject
        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        try {
            JSONObject params = new JSONObject();
            params.put("mch_no", jsonParam.getString("mch_no"));
            params.put("app_id", jsonParam.getString("app_id"));
            params.put("nonce_str", jsonParam.getString("nonce_str"));
            params.put("trade_type",jsonParam.getString("trade_type"));
            params.put("total_fee",jsonParam.getString("total_fee"));
            params.put("trans_no",jsonParam.getString("trans_no"));
            params.put("thd_trans_no",jsonParam.getString("thd_trans_no"));
            params.put("out_trade_no",jsonParam.getString("out_trade_no"));
            params.put("trade_status",jsonParam.getString("trade_status"));
            params.put("return_code",jsonParam.getString("return_code"));
            params.put("return_msg",jsonParam.getString("return_msg"));
            params.put("trade_desc",jsonParam.getString("trade_desc"));
            params.put("sign",jsonParam.getString("sign"));
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
                _log.info("====== 完成处理猪肉支付回调通知 ======");
            }
            respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证猪肉支付通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String memberid = params.getString("mch_no");  //商户号
        String app_id = params.getString("app_id"); //应用id
        String nonceStr = params.getString("nonce_str"); //随机字符串
        String trade_type = params.getString("trade_type"); // 支付类型
        String total_fee = params.getString("total_fee");//支付金额
        String trans_no = params.getString("trans_no");//交易流水号
        String thd_trans_no =  params.getString("thd_trans_no");//三方交易流水号
        String out_trade_no = params.getString("out_trade_no");//支付订单号
        String trade_status = params.getString("trade_status");//交易状态
        String return_code = params.getString("return_code");//返回状态码
        String return_msg = params.getString("return_msg");//交易状态
        String trade_desc = params.getString("trade_desc"); //回调描述

        String sign = params.getString("sign");  //签名
        // 查询payOrder记录
        String payOrderId = out_trade_no;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // 验证签名
        SortedMap<String, String> signMap = new TreeMap<String, String>();
        signMap.put("mch_no", memberid);
        signMap.put("app_id", app_id);
        signMap.put("nonce_str", nonceStr);
        signMap.put("trade_type", trade_type);
        signMap.put("total_fee",total_fee);
        signMap.put("trans_no",trans_no);
        signMap.put("out_trade_no", out_trade_no);
        signMap.put("trade_status", trade_status);
        signMap.put("return_code", return_code);
        signMap.put("return_msg", return_msg);
        signMap.put("trade_desc",trade_desc);
        signMap.put("thd_trans_no",thd_trans_no);
        // md5 加密
        String md5 = PayDigestUtil.getSign(signMap, channelPayConfig.getmD5Key());
        if (!md5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "验证签名失败");
            return false;
        }
        // 核对金额

        long dbPayAmt = payOrder.getAmount().longValue();
        long resAmount = new BigDecimal(total_fee).longValue();
        if (dbPayAmt != resAmount) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", total_fee, payOrderId);
            payContext.put("retMsg", "金额不一致");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }

}
