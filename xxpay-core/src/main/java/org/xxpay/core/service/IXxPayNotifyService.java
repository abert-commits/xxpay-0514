package org.xxpay.core.service;

/**
 * @author: dingzhiwei
 * @date: 2018/5/29
 * @description:
 */
public interface IXxPayNotifyService {

	/**
	 * 发送支付订单通知
	 * @param payOrderId
	 */
	void executePayNotify(String payOrderId);

}
