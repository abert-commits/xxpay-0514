package org.xxpay.pay.mq.rocketmq.batch;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.bean.BatchConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 批量多线程MQ
 * 暂时不使用
 */
//项目中加上 @Configuration 注解，这样服务启动时consumer也启动了
public class BatchConsumerClient {

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private BatchDemoMessageListener messageListener;

   /* @Bean(initMethod = "start", destroyMethod = "shutdown")
    public BatchConsumerBean buildBatchConsumer() {
        BatchConsumerBean batchConsumerBean = new BatchConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        batchConsumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, BatchMessageListener> subscriptionTable = new HashMap<Subscription, BatchMessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(rocketMqConfig.getTopic());
        subscription.setExpression(rocketMqConfig.getTag());
        subscriptionTable.put(subscription, messageListener);
        //订阅多个topic如上面设置

        batchConsumerBean.setSubscriptionTable(subscriptionTable);
        return batchConsumerBean;
    }*/

}
