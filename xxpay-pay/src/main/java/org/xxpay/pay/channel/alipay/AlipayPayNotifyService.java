package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.LockUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.StrUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.swiftpay.util.XmlUtils;
import org.xxpay.pay.mq.BaseNotify4CashColl;
import org.xxpay.pay.mq.rocketmq.normal.BaseNotify5CashColl;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
@Service
public class AlipayPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(AlipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    @Autowired
    private BaseNotify5CashColl baseNotify5CashColl;

    @Autowired
    private AlipayPaymentService alipayPaymentService;

    @Autowired
    private AlitstpayPaymentService alitstpayPaymentService;

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理支付宝支付回调】";
        _log.info("====== 开始处理支付宝支付回调通知 ======");
        Map params = null;
        if (notifyData instanceof Map) {
            params = (HashMap) notifyData;
        } else if (notifyData instanceof HttpServletRequest) {
            params = buildNotifyData((HttpServletRequest) notifyData);
        }

        _log.info("{}请求数据:{}", logPrefix, params);
        // 构建返回对象
        JSONObject retObj = buildRetObj();
        if (params == null || params.isEmpty()) {
            retObj.put(PayConstant.RESPONSE_RESULT, WxPayNotifyResponse.fail("请求数据为空"));
            return retObj;
        }

        if (params.containsKey("service") && params.get("service").equals("alipay.service.check")) {
            //应用网关验证
            return AliLifeNumberCheck(params);
        }


        //如果通知消息是现金红包的
        if (params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.order.changed")
                || params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.uni.transfer")) {

            return CashRedEnvelopeNotify(params, retObj, logPrefix);
        }

        if (params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.refund.success")) {
            //退款通知
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
            return retObj;
        }


        //普通支付回调
        return OrdinaryPaymentNotify(params, retObj, logPrefix);
    }

    static String KEY = "Notify_";

    /**
     * 普通支付流程回调
     *
     * @param params
     * @param retObj
     * @param logPrefix
     * @return
     */
    private JSONObject OrdinaryPaymentNotify(Map params, JSONObject retObj, String logPrefix)

    {
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        payContext.put("parameters", params);
        if (!verifyAliPayParams(payContext)) {
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}验证支付通知数据及签名通过", logPrefix);
        String trade_status = params.get("trade_status").toString();        // 交易状态
        // 支付状态成功或者完成
        int updatePayOrderRows = 0;
        if (trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_SUCCESS) ||
                trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_FINISHED)) {
            payOrder = (PayOrder) payContext.get("payOrder");
            byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期

            if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                //加锁 休眠进程 避免大并发 导致掉单
                LockUtil.lock(KEY + payOrder.getMchId(), 6);
                try {
                    updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId(), StrUtil.toString(params.get("trade_no"), null));
                } catch (Exception e) {

                } finally {
                    //释放锁
                    LockUtil.unlock(KEY + payOrder.getMchId());
                }

                if (updatePayOrderRows != 1) {
                    _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    _log.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                    retObj.put("resResult", PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                    return retObj;
                }

                _log.info("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                baseNotify5CashColl.doNotify(payOrder.getPayOrderId());

            }

        } else {
            // 其他状态
            _log.info("{}支付状态trade_status={},不做业务处理", logPrefix, trade_status);
            _log.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
            return retObj;
        }

//        if (updatePayOrderRows > 0) {
//            baseNotify4MchPay.doNotify(payOrder, true);
//        }
        _log.info("====== 完成处理支付宝支付回调通知 ======");
        retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
        return retObj;
    }


    /**
     * 支付宝生活号应用网关验证
     *
     * @param params
     * @return
     */
    private JSONObject AliLifeNumberCheck(Map params) {

        // 构建返回对象
        JSONObject retObj = buildRetObj();
        try {

            String resString = params.get("biz_content").toString();
            Map<String, String> bizParams = XmlUtils.toMap(resString.getBytes(), "utf-8");
            String appId = bizParams.get("appid").toString();
            PayPassageAccount model = new PayPassageAccount();
            model.setPassageMchId(appId);
            List<PayPassageAccount> list = rpcCommonService.rpcPayPassageAccountService.selectAll(model);
            if (list == null && list.size() == 0) {
                retObj.put(PayConstant.RESPONSE_RESULT, WxPayNotifyResponse.fail("未获取到相关应用ID"));
                return retObj;
            }

            model = list.get(0);
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(model.getParam());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(DateUtil.getRevTime(), channelPayConfig.getmD5Key());

            Boolean verify_result = AlipaySignature.rsaCertCheckV2(params, alipayCertPublicKeyPath, "GBK", "RSA2");
            if (!verify_result) {
                retObj.put(PayConstant.RESPONSE_RESULT, WxPayNotifyResponse.fail("verify sign fail"));
                return retObj;
            }
            StringBuilder sb = new StringBuilder();
            String publicKey = AlipaySignature.getAlipayPublicKey(alipayCertPublicKeyPath);
            sb.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
            sb.append("<alipay>");
            String myEncrypted = AlipaySignature.rsaEncrypt(resString, publicKey, "GBK");
            sb.append(" <response>");
            sb.append("<biz_content>" + myEncrypted + "</biz_content>");
            sb.append("<success>true</success>");
            sb.append(" </response>");
            String mySign = AlipaySignature.rsaSign(myEncrypted, channelPayConfig.getRsaprivateKey(), "GBK", "RSA2");
            sb.append("<sign_type>");
            sb.append("RSA2");
            sb.append("</sign_type>");
            sb.append("</alipay>");

            retObj.put(PayConstant.RESPONSE_RESULT, sb.toString());
            return retObj;
        } catch (Exception ex) {
            retObj.put(PayConstant.RESPONSE_RESULT, "系统内部发生异常");
            return retObj;
        }
    }


    /**
     * 支付宝现金红包 资金单据状态变更通知
     *
     * @param params
     * @param retObj
     * @param logPrefix
     * @return
     */
    private JSONObject CashRedEnvelopeNotify(Map params, JSONObject retObj, String logPrefix) {
        Map<String, Object> payContext = new HashMap();

        payContext.put("parameters", params);
        if (!verifyAliPayParams(payContext)) {
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
            return retObj;
        }

        JSONObject myJsonOBJ = JSONObject.parseObject(params.get("biz_content").toString());
        _log.info("{}验证支付通知数据及签名通过", logPrefix);
        String trade_status = myJsonOBJ.getString("status").toString();        // 交易状态
        String biz_scene = myJsonOBJ.getString("biz_scene").toString();
        String channelOrderNo = myJsonOBJ.getString("order_id").toString();
        PayOrder payOrder = (PayOrder) payContext.get("payOrder");
        if (payOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS) {
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
            return retObj;
        }

        switch (biz_scene) {
            //C2C现金红包-发红包
            case "PERSONAL_PAY":
                if (trade_status.equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                    //调用C2C分发红包方法
                    payOrder.setChannelOrderNo(channelOrderNo);
                    //分发现金红包,记录领红包人成功和失败笔数
                    JSONObject jsonObject = alitstpayPaymentService.transunitransferByTST(payOrder);
                    if (!jsonObject.getString(PayConstant.RETURN_PARAM_RETCODE).equals(PayConstant.RETURN_VALUE_SUCCESS))
                    {
                        retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_VALUE_FAIL);
                        break;
                    }
                }

                if (trade_status.equals("WAIT_PAY")) {
                    payOrder.setChannelOrderNo(channelOrderNo);
                    rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), channelOrderNo);
                }

                retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
                break;
            case "PERSONAL_COLLECTION":
                int updatePayOrderRows;
                if (trade_status.equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                    //领红包通知
                    byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期
                    if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                        updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId(), StrUtil.toString(myJsonOBJ.get("order_id"), null));
                        if (updatePayOrderRows != 1) {
                            _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                            _log.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                            retObj.put("resResult", PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                            return retObj;
                        }

                        _log.info("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                        payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                        baseNotify4MchPay.doNotify(payOrder, true);
                    }
                }
                retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
                break;
        }

        if (params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.refund.success")) {

            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
//            int updatePayOrderRows;
//            //进来这边表示是红包超时退款通知
//            if (trade_status.equals(PayConstant.RETURN_ALIPAY_VALUE_SUCCESS)) {
//                //修改订单状态为支付失败
//                updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Fail(payOrder.getPayOrderId());
//                if (updatePayOrderRows > 0) {
//                    payOrder.setStatus(PayConstant.TRANS_STATUS_FAIL);
//                    //通知下游支付失败
//                    baseNotify4MchPay.doNotify(payOrder, true);
//                }
//            }
        }

        _log.info("====== 完成处理支付宝支付回调通知 ======");
        return retObj;
    }


    @Override
    public JSONObject doReturn(Object notifyData) {
        String logPrefix = "【处理支付宝同步跳转】";
        _log.info("====== 开始处理支付宝同步跳转 ======");

        Map params = null;
        if (notifyData instanceof Map) {
            params = (HashMap) notifyData;
        } else if (notifyData instanceof HttpServletRequest) {
            params = buildNotifyData((HttpServletRequest) notifyData);
        }
        _log.info("{}请求数据:{}", logPrefix, params);

        // 构建返回对象
        JSONObject retObj = buildRetObj();
        if (params == null || params.isEmpty()) {
            retObj.put(PayConstant.RESPONSE_RESULT, WxPayNotifyResponse.fail("请求数据为空"));
            return retObj;
        }
        Map<String, Object> payContext = new HashMap();

        payContext.put("parameters", params);
        if (!verifyAliPayParams(payContext)) {
            retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
            return retObj;
        }
        _log.info("{}验证支付通知数据及签名通过", logPrefix);

        PayOrder payOrder = (PayOrder) payContext.get("payOrder");

        _log.info("====== 完成处理支付宝同步跳转 ======");
        String url = baseNotify4MchPay.createNotifyUrl(payOrder, "1");
        retObj.put(PayConstant.RESPONSE_RESULT, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
        retObj.put(PayConstant.JUMP_URL, url);
        return retObj;
    }

    /**
     * 解析支付宝回调请求的数据
     *
     * @param request
     * @return
     */
    public Map buildNotifyData(HttpServletRequest request) {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        return params;
    }

    /**
     * 验证支付宝支付通知参数
     *
     * @return
     */
    public boolean verifyAliPayParams(Map<String, Object> payContext) {
        Map<String, String> params = (Map<String, String>) payContext.get("parameters");
        String out_trade_no = "";  // 商户订单号
        String total_amount = "";  //订单金额
        //如果通知消息是现金红包的
        if (params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.order.changed")
                || params.containsKey("msg_method") && params.get("msg_method").equals("alipay.fund.trans.uni.transfer")) {

            JSONObject biz_contentJson = JSONObject.parseObject(params.get("biz_content"));
            out_trade_no = biz_contentJson.getString("out_biz_no");
            total_amount = biz_contentJson.getString("trans_amount");
        } else {
            out_trade_no = params.get("out_trade_no");
            total_amount = params.get("total_amount");
        }

        // 支付金额
        if (org.springframework.util.StringUtils.isEmpty(out_trade_no)) {
            _log.error("AliPay Notify parameter out_trade_no is empty. out_trade_no={}", out_trade_no);
            payContext.put("retMsg", "out_trade_no is empty");
            return false;
        }

        if (org.springframework.util.StringUtils.isEmpty(total_amount)) {
            _log.error("AliPay Notify parameter total_amount is empty. total_fee={}", total_amount);
            payContext.put("retMsg", "total_amount is empty");
            return false;
        }
        String errorMessage;
        // 查询payOrder记录
        String payOrderId = out_trade_no;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        boolean verify_result = false;
        try {
            AlipayConfig alipayConfig = new AlipayConfig(getPayParamAll(payOrder));
            String alipayCertPublicKey = GetAlipayCertPublicKey(payOrderId, alipayConfig.getAppId());
            String sign = (String) params.get("sign").replace(" ", "+");
            params.put("sign", sign);
            String content = AlipaySignature.getSignCheckContentV1(params);

            verify_result = AlipaySignature.rsaCertCheck(content, sign, alipayCertPublicKey, AlipayConfig.CHARSET, AlipayConfig.SIGNTYPE);
        } catch (AlipayApiException e) {
            _log.error(e, "AlipaySignature.rsaCheckV1 error");
        }

        if (!verify_result) {
            errorMessage = "rsaCheckV1 failed.";
            _log.error("AliPay Notify parameter {}", errorMessage);
            payContext.put("retMsg", errorMessage);
            return false;
        }

        // 核对金额
        long aliPayAmt = new BigDecimal(total_amount).movePointRight(2).longValue();
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != aliPayAmt) {
            _log.error("db payOrder record payPrice not equals total_amount. total_amount={},payOrderId={}", total_amount, payOrderId);
            payContext.put("retMsg", "");
            return false;
        }
        payContext.put("payOrder", payOrder);
        return true;
    }
}
