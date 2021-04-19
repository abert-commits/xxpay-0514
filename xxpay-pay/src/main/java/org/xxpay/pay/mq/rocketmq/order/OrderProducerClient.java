package org.xxpay.pay.mq.rocketmq.order;

import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;

@Configuration
public class OrderProducerClient {

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public OrderProducerBean buildOrderProducer() {
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(rocketMqConfig.getMqPropertie());
        return orderProducerBean;
    }

}
