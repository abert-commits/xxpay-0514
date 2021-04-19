package org.xxpay.pay.mq.rocketmq;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqConfig {
    public static final String PAY_NOTIFY_QUEUE_NAME = "pay_notify_queue";

    public static final String MCH_NOTIFY_QUEUE_NAME = "queue_notify_mch";

    public static final String MCH_PAY_NOTIFY_QUEUE_NAME = "queue_notify_mch_pay";

    public static final String MCH_TRANS_NOTIFY_QUEUE_NAME = "queue_notify_mch_trans";

    public static final String MCH_REFUND_NOTIFY_QUEUE_NAME = "queue_notify_mch_refund";

    public static final String MCH_AGENTPAY_NOTIFY_QUEUE_NAME = "queue_notify_mch_agentpay";

    public static final String TRANS_NOTIFY_QUEUE_NAME = "queue_notify_trans";

    public static final String REFUND_NOTIFY_QUEUE_NAME = "queue_notify_refund";

    public static final String PAY_QUERY_QUEUE_NAME = "queue_pay_query";

    public static final String TRANS_QUERY_QUEUE_NAME = "queue_trans_query";

    public static final String CASH_COLL_QUEUE_NAME = "queue_cash_coll";

    public static final String CASH_COLL_QUEUE_QUERY_NAME = "queue_cash_coll_query";

    public static final String MCH_PAY_NOTIFY_QUEUE_NAME_PDD = "queue_notify_pdd_pay";

    public static final String QUEUE_RED_ENVELOPE = "queue_red_envelope";

    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String groupId;
    private String tag;
    private String orderTopic;
    private String orderGroupId;
    private String orderTag;

    public Properties getMqPropertie() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getNameSrvAddr() {
        return nameSrvAddr;
    }

    public void setNameSrvAddr(String nameSrvAddr) {
        this.nameSrvAddr = nameSrvAddr;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOrderTopic() {
        return orderTopic;
    }

    public void setOrderTopic(String orderTopic) {
        this.orderTopic = orderTopic;
    }

    public String getOrderGroupId() {
        return orderGroupId;
    }

    public void setOrderGroupId(String orderGroupId) {
        this.orderGroupId = orderGroupId;
    }

    public String getOrderTag() {
        return orderTag;
    }

    public void setOrderTag(String orderTag) {
        this.orderTag = orderTag;
    }
}
