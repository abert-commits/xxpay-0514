package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;

/**
 * 资金归集
 */
public interface CashCollInterface {

    JSONObject coll(PayOrder payOrder) throws Exception;

    JSONObject relationbind(PayCashCollConfig payProduct);

    JSONObject collRedEnvelope(PayOrder payOrder) throws Exception;

}
