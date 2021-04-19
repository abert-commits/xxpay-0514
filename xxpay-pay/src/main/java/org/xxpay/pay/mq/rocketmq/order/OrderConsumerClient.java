package org.xxpay.pay.mq.rocketmq.order;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 单线程 mq
 */
//项目中加上 @Configuration 注解，这样服务启动时consumer也启动了
public class OrderConsumerClient {

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private OrderDemoMessageListener messageListener;

    /*@Bean(initMethod = "start", destroyMethod = "shutdown")
    public OrderConsumerBean buildOrderConsumer() {
        OrderConsumerBean orderConsumerBean = new OrderConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getOrderGroupId());
        orderConsumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<Subscription, MessageOrderListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(rocketMqConfig.getOrderTopic());
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, messageListener);
        //订阅多个topic如上面设置

        orderConsumerBean.setSubscriptionTable(subscriptionTable);
        return orderConsumerBean;
    }*/

}
