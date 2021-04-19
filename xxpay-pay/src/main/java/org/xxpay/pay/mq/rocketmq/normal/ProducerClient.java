package org.xxpay.pay.mq.rocketmq.normal;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;

@Configuration
public class ProducerClient {

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean buildProducer() {
        ProducerBean producer = new ProducerBean();
        producer.setProperties(rocketMqConfig.getMqPropertie());
        return producer;
    }






}
