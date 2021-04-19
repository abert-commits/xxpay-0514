package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.*;
import org.xxpay.pay.mq.Mq4MchPayNotify;
import org.xxpay.pay.service.RpcCommonService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 商户支付通知处理基类
 * @date 2017-11-01
 * @Copyright: www.xxpay.org
 */
@Component
public class BaseNotify5MchPay {

    private static final MyLog _log = MyLog.getLog(BaseNotify5MchPay.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private Mq5MchPayNotify mq5MchPayNotify;

    /**
     * 校验通道是否关闭
     * @param payOrder
     * @return
     */
    public boolean checkPayPassageStatus(PayOrder payOrder) {
        MchInfo mchInfo = rpcCommonService.rpcMchInfoService.findByMchId(payOrder.getMchId());
        MchGroupPayPassage mchGroupPayPassage= rpcCommonService.rpcMchGroupPayPassageService.findByMchGroupIdAndProductId(new Long(mchInfo.getGroupId()), payOrder.getProductId());
        return mchGroupPayPassage.getStatus() == MchConstant.PUB_YES;
    }

    /**
     * 创建响应URL
     *
     * @param payOrder
     * @param backType 1：前台页面；2：后台接口
     * @return
     */
    public String createNotifyUrl(PayOrder payOrder, String backType) {
        if ("1".equals(backType) && StringUtils.isBlank(payOrder.getReturnUrl())) {
            return null;
        }
        if ("2".equals(backType) && StringUtils.isBlank(payOrder.getNotifyUrl())) {
            return null;
        }
        Long mchId = payOrder.getMchId();
        MchInfo mchInfo = rpcCommonService.rpcMchInfoService.findByMchId(mchId);
        String key = mchInfo.getPrivateKey();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("payOrderId", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));            // 支付订单号
        paramMap.put("mchId", ObjectUtils.defaultIfNull(payOrder.getMchId(), ""));                        // 商户ID
        paramMap.put("appId", ObjectUtils.defaultIfNull(payOrder.getAppId(), ""));                        // 应用ID
        paramMap.put("productId", ObjectUtils.defaultIfNull(payOrder.getProductId(), ""));                // 产品ID
        paramMap.put("mchOrderNo", ObjectUtils.defaultIfNull(payOrder.getMchOrderNo(), ""));            // 商户订单号
        paramMap.put("amount", ObjectUtils.defaultIfNull(payOrder.getAmount(), ""));                    // 支付金额
        paramMap.put("income", ObjectUtils.defaultIfNull(payOrder.getMchIncome(), ""));                // 入账金额
        paramMap.put("status", ObjectUtils.defaultIfNull(payOrder.getStatus(), ""));                    // 支付状态
        paramMap.put("channelOrderNo", ObjectUtils.defaultIfNull(payOrder.getChannelOrderNo(), ""));    // 渠道订单号
        paramMap.put("param1", ObjectUtils.defaultIfNull(payOrder.getParam1(), ""));                    // 扩展参数1
        paramMap.put("param2", ObjectUtils.defaultIfNull(payOrder.getParam2(), ""));                    // 扩展参数2
        paramMap.put("paySuccTime", "".equals(ObjectUtils.defaultIfNull(payOrder.getPaySuccTime(), "")) ? "" : payOrder.getPaySuccTime().getTime());            // 支付成功时间
        paramMap.put("backType", ObjectUtils.defaultIfNull(backType, ""));
        // 先对原文签名
        String reqSign = PayDigestUtil.getSign(paramMap, key);
        paramMap.put("sign", reqSign);   // 签名
        // 生成参数串
        String param = XXPayUtil.genUrlParams(paramMap);
        StringBuffer sb = new StringBuffer();
        if ("1".equals(backType)) {
            sb.append(payOrder.getReturnUrl()).append("?").append(param);
        } else {
            sb.append(payOrder.getNotifyUrl()).append("?").append(param);
        }
        return sb.toString();
    }

    /**
     * 处理支付结果后台服务器通知
     */
    public void doNotify(PayOrder payOrder, boolean isFirst) {
        //todo  关闭通道不在通道下游商户
//        boolean flag = checkPayPassageStatus(payOrder);
//        if (!flag) return;
        _log.info(">>>>>> PAY开始回调通知业务系统 <<<<<<");
        // 发起后台通知业务系统
        JSONObject object = createNotifyInfo(payOrder, isFirst);
        try {
            mq5MchPayNotify.Send(object.toJSONString());
        } catch (Exception e) {
            _log.error(e, "payOrderId=%s,sendMessage error.", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));
        }
        _log.info(">>>>>> PAY回调通知业务系统完成 <<<<<<");
    }

    public JSONObject createNotifyInfo(PayOrder payOrder, boolean isFirst) {
        // 查询最新订单数据
        payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrder.getPayOrderId());
        String url = createNotifyUrl(payOrder, "2");
        MchNotify mchNotify = rpcCommonService.rpcMchNotifyService.findByOrderId(payOrder.getPayOrderId());
        isFirst = mchNotify == null ? true : false;

        if (isFirst) {
            int result = rpcCommonService.rpcMchNotifyService.insertSelectiveOnDuplicateKeyUpdate(payOrder.getPayOrderId(), payOrder.getMchId(), payOrder.getAppId(), payOrder.getMchOrderNo(), PayConstant.MCH_NOTIFY_TYPE_PAY, url);
            _log.info("增加商户通知记录,orderId={},result:{}", payOrder.getPayOrderId(), result);
        }
        int count = 0;
        if (!isFirst) {
            mchNotify = rpcCommonService.rpcMchNotifyService.findByOrderId(payOrder.getPayOrderId());
            if (mchNotify != null) count = mchNotify.getNotifyCount();
        }
        JSONObject object = new JSONObject();
        object.put("method", "GET");
        object.put("url", url);
        object.put("orderId", payOrder.getPayOrderId());
        object.put("count", count);
        object.put("createTime", System.currentTimeMillis());
        return object;
    }

}
