package org.xxpay.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.service.IXxPayNotifyService;
import org.xxpay.pay.mq.BaseNotify4MchPay;
import org.xxpay.pay.mq.Mq4MchAgentpayNotify;
import org.xxpay.pay.mq.Mq4MchPayNotify;
import org.xxpay.pay.mq.rocketmq.normal.BaseNotify5MchPay;
import org.xxpay.pay.service.RpcCommonService;

/**
 * @author: dingzhiwei
 * @date: 2018/5/29
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IXxPayNotifyService", version = "1.0.0", retries = -1)
public class XxPayNotifyServiceImpl implements IXxPayNotifyService {

    private static final MyLog _log = MyLog.getLog(XxPayNotifyServiceImpl.class);

    @Autowired
    private RpcCommonService rpcCommonService;

	@Autowired
	public BaseNotify5MchPay baseNotify5MchPay;
    
	/**
	 * 发送支付订单通知
	 * @param payOrderId
	 */
	public void executePayNotify(String payOrderId) {
		_log.info(">>>>>> 调取rpc补发支付通知,payOrderId：{}", payOrderId);
		PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
		baseNotify5MchPay.doNotify(payOrder, true);
		_log.info(">>>>>> 调取rpc补发支付通知完成  <<<<<<");
	}
}
