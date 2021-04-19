package org.xxpay.pay.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.MchApp;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.MchNotify;
import org.xxpay.core.entity.RefundOrder;
import org.xxpay.pay.service.RpcCommonService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 商户转账通知处理基类
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-11-01
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Component
public class BaseNotify4MchRefund {

	private static final MyLog _log = MyLog.getLog(BaseNotify4MchRefund.class);

	@Autowired
	private Mq4MchRefundNotify mq4MchRefundNotify;

	@Autowired
	private RpcCommonService rpcCommonService;

	/**
	 * 创建响应URL
	 * @param refundOrder
	 * @param backType 1：前台页面；2：后台接口
	 * @return
	 */
	public String createNotifyUrl(RefundOrder refundOrder, String backType) {
		Long mchId = refundOrder.getMchId();
		MchInfo mchInfo = rpcCommonService.rpcMchInfoService.findByMchId(mchId);
		String key = mchInfo.getPrivateKey();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("refundOrderId", ObjectUtils.defaultIfNull(refundOrder.getRefundOrderId(), ""));           // 退款订单号
		paramMap.put("mchId", ObjectUtils.defaultIfNull(refundOrder.getMchId(), ""));                      	 	// 商户ID
		paramMap.put("appId", ObjectUtils.defaultIfNull(refundOrder.getAppId(), ""));							// 应用ID
		paramMap.put("mchRefundNo", ObjectUtils.defaultIfNull(refundOrder.getMchRefundNo(), ""));       		// 商户订单号
		paramMap.put("refundAmount", ObjectUtils.defaultIfNull(refundOrder.getRefundAmount(), ""));             // 退款金额
		paramMap.put("status", ObjectUtils.defaultIfNull(refundOrder.getStatus(), ""));               			// 退款状态
		paramMap.put("channelOrderNo", ObjectUtils.defaultIfNull(refundOrder.getChannelOrderNo(), "")); 		// 渠道订单号
		paramMap.put("param1", ObjectUtils.defaultIfNull(refundOrder.getParam1(), ""));               		   	// 扩展参数1
		paramMap.put("param2", ObjectUtils.defaultIfNull(refundOrder.getParam2(), ""));               		   	// 扩展参数2
		paramMap.put("refundSuccTime", "".equals(ObjectUtils.defaultIfNull(refundOrder.getRefundSuccTime(), "")) ? "" : refundOrder.getRefundSuccTime().getTime());			// 退款成功时间
		paramMap.put("backType", backType==null ? "" : backType);
		// 先对原文签名
		String reqSign = PayDigestUtil.getSign(paramMap, key);
		paramMap.put("sign", reqSign);   // 签名
		// 签名后再对有中文参数编码
		try {
			paramMap.put("param1", URLEncoder.encode(ObjectUtils.defaultIfNull(refundOrder.getParam1(), ""), PayConstant.RESP_UTF8));
			paramMap.put("param2", URLEncoder.encode(ObjectUtils.defaultIfNull(refundOrder.getParam2(), ""), PayConstant.RESP_UTF8));
		}catch (UnsupportedEncodingException e) {
			_log.error("URL Encode exception.", e);
			return null;
		}
		String param = XXPayUtil.genUrlParams(paramMap);
		StringBuffer sb = new StringBuffer();
		sb.append(refundOrder.getNotifyUrl()).append("?").append(param);
		return sb.toString();
	}

	/**
	 * 处理商户转账后台服务器通知
	 */
	public void doNotify(RefundOrder refundOrder, boolean isFirst) {
		_log.info(">>>>>> REFUND开始回调通知业务系统 <<<<<<");
		// 发起后台通知业务系统
		JSONObject object = createNotifyInfo(refundOrder, isFirst);
		try {
			mq4MchRefundNotify.send(object.toJSONString());
		} catch (Exception e) {
			_log.error(e, "refundOrderId=%s,sendMessage error.", ObjectUtils.defaultIfNull(refundOrder.getRefundOrderId(), ""));
		}
		_log.info(">>>>>> REFUND回调通知业务系统完成 <<<<<<");
	}

	public JSONObject createNotifyInfo(RefundOrder refundOrder, boolean isFirst) {
		String url = createNotifyUrl(refundOrder, "2");
		if(isFirst) {
			int result = rpcCommonService.rpcMchNotifyService.insertSelectiveOnDuplicateKeyUpdate(refundOrder.getRefundOrderId(), refundOrder.getMchId(), refundOrder.getAppId(), refundOrder.getMchRefundNo(), PayConstant.MCH_NOTIFY_TYPE_REFUND, url);
			_log.info("增加商户通知记录,orderId={},result:{}", refundOrder.getRefundOrderId(), result);
		}
		int count = 0;
		if(!isFirst) {
			MchNotify mchNotify = rpcCommonService.rpcMchNotifyService.findByOrderId(refundOrder.getRefundOrderId());
			if(mchNotify != null) count = mchNotify.getNotifyCount();
		}
		JSONObject object = new JSONObject();
		object.put("method", "GET");
		object.put("url", url);
		object.put("orderId", refundOrder.getRefundOrderId());
		object.put("count", count);
		object.put("createTime", System.currentTimeMillis());
		return object;
	}

}
