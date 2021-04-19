package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xxpay.core.entity.PayOrder;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
public interface PaymentInterface {

    JSONObject pay(PayOrder payOrder);

    JSONObject query(PayOrder payOrder);

    JSONObject close(PayOrder payOrder);

    /**
     * 如果上游通道对订单ID格式有特殊要求
     * 那么实现该接口按照上游通道ID格式生成
     * @return
     */
    String getOrderId(PayOrder payOrder);

    /**
     * 如果需要变更金额，需要实现该方法
     * @return
     */
    Long getAmount(PayOrder payOrder);

}
