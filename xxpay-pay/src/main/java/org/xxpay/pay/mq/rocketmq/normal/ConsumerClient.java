package org.xxpay.pay.mq.rocketmq.normal;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 多线程mq
 */
@Configuration
//项目中加上 @Configuration 注解，这样服务启动时consumer也启动了
public class ConsumerClient {


    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private Mq5CashColl mq5CashColl;

    @Autowired
    private Mq5MchAgentpayNotify mq5MchAgentpayNotify;

    @Autowired
    private Mq5MchPayNotify mq5MchPayNotify;

    @Autowired
    private Mq5MchRefundNotify mq5MchRefundNotify;

    @Autowired
    private Mq5MchTransNotify mq5MchTransNotify;

    @Autowired
    private Mq5PayQuery mq5PayQuery;

    @Autowired
    private Mq5PinduoduoNotify mq5PinduoduoNotify;

    @Autowired
    private Mq5RefundNotify mq5RefundNotify;

    @Autowired
    private Mq5TransNotify mq5TransNotify;

    @Autowired
    private Mq5TransQuery mq5TransQuery;

    @Autowired
    private Mq5CashCollQuery mq5CashCollQuery;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5CashCollConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为10个 10为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "5");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        //分账
        Subscription queue_cash_coll = new Subscription();
        queue_cash_coll.setTopic(RocketMqConfig.CASH_COLL_QUEUE_NAME);
        queue_cash_coll.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(queue_cash_coll, mq5CashColl);

      /*  Subscription queue_notify_mch_agentpay = new Subscription();
        queue_notify_mch_agentpay.setTopic(RocketMqConfig.MCH_AGENTPAY_NOTIFY_QUEUE_NAME);
        queue_notify_mch_agentpay.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(queue_notify_mch_agentpay, mq5MchAgentpayNotify);*/

        Subscription queue_notify_mch_pay = new Subscription();
        queue_notify_mch_pay.setTopic(RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME);
        queue_notify_mch_pay.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(queue_notify_mch_pay, mq5MchPayNotify);


        Subscription queue_cash_coll_query = new Subscription();
        queue_cash_coll_query.setTopic(RocketMqConfig.CASH_COLL_QUEUE_QUERY_NAME);
        queue_cash_coll_query.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(queue_cash_coll_query, mq5CashCollQuery);


        //订单查询MQ,查询成功后扭转订单状态+通知下游
        Subscription pay_query_queue_name = new Subscription();
        pay_query_queue_name.setTopic(RocketMqConfig.PAY_QUERY_QUEUE_NAME);
        pay_query_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(pay_query_queue_name, mq5PayQuery);

      /*  Subscription mch_refund_notify_queue_name = new Subscription();
        mch_refund_notify_queue_name.setTopic(RocketMqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME);
        mch_refund_notify_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(mch_refund_notify_queue_name, mq5MchRefundNotify);


        Subscription mch_trans_notify_queue_name = new Subscription();
        mch_trans_notify_queue_name.setTopic(RocketMqConfig.MCH_TRANS_NOTIFY_QUEUE_NAME);
        mch_trans_notify_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(mch_trans_notify_queue_name, mq5MchTransNotify);


        Subscription pay_query_queue_name = new Subscription();
        pay_query_queue_name.setTopic(RocketMqConfig.PAY_QUERY_QUEUE_NAME);
        pay_query_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(pay_query_queue_name, mq5PayQuery);

        Subscription mch_pay_notify_queue_name_pdd = new Subscription();
        mch_pay_notify_queue_name_pdd.setTopic(RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME_PDD);
        mch_pay_notify_queue_name_pdd.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(mch_pay_notify_queue_name_pdd, mq5PinduoduoNotify);


        Subscription queue_notify_refund = new Subscription();
        queue_notify_refund.setTopic(RocketMqConfig.REFUND_NOTIFY_QUEUE_NAME);
        queue_notify_refund.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(queue_notify_refund, mq5RefundNotify);


        Subscription trans_notify_queue_name = new Subscription();
        trans_notify_queue_name.setTopic(RocketMqConfig.TRANS_NOTIFY_QUEUE_NAME);
        trans_notify_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(trans_notify_queue_name, mq5TransNotify);



        Subscription trans_query_queue_name = new Subscription();
        trans_query_queue_name.setTopic(RocketMqConfig.TRANS_QUERY_QUEUE_NAME);
        trans_query_queue_name.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(trans_query_queue_name, mq5TransQuery);*/
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

   /* @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5MchAgentpayNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.MCH_AGENTPAY_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5MchAgentpayNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5MchPayNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5MchPayNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5MchRefundNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5MchRefundNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5MchTransNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.MCH_TRANS_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5MchTransNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5PayQueryConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.PAY_QUERY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5PayQuery);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5PinduoduoNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME_PDD);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5PinduoduoNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5RefundNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.REFUND_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5RefundNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5TransNotifyConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.TRANS_NOTIFY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5TransNotify);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean mq5TransQueryConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        //配置文件
        Properties properties = rocketMqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, rocketMqConfig.getGroupId());
        //将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        //订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<Subscription, MessageListener>();
        Subscription subscription = new Subscription();
        subscription.setTopic(RocketMqConfig.TRANS_QUERY_QUEUE_NAME);
        subscription.setExpression(rocketMqConfig.getOrderTag());
        subscriptionTable.put(subscription, mq5TransQuery);
        //订阅多个topic如上面设置
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }
*/
}
