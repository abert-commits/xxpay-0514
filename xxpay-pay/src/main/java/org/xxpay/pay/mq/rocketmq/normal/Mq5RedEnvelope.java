package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.TelegramUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.CashCollInterface;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component

public class Mq5RedEnvelope implements MessageListener {
    private static final MyLog _log = MyLog.getLog(Mq5RedEnvelope.class);
    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProducerBean producerBean;

    //调用Mq
    @Autowired
    private Mq5CashCollQuery mq5CashCollQuery;

    @Autowired
    public BaseNotify5MchPay baseNotify5MchPay;

    //消费者
    @Override
    public Action consume(Message message, ConsumeContext context) {
        _log.info("处理现金红包分发任务.message={}", message);
        String msg = null;
        long startTime = System.currentTimeMillis();
        try {
            msg = new String(message.getBody(), "UTF-8");
            _log.info("处理现金红包分发任务.msg={}", message);
            JSONObject msgObj = JSON.parseObject(msg);
            String payOrderId = msgObj.getString("payOrderId");

            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            //判断当前订单是否已经有分发红包的记录，如果有的话，表示已经执行了分发红包的mq，这里不再重复执行，留给运营在后台补发。
            List<PayOrderCashCollRecord> list = rpcCommonService.rpcPayOrderCashCollRecordService.selectByOrderId(payOrderId);

            //if (lis)
            CashCollInterface cashCollInterface = (CashCollInterface) SpringUtil.getBean("alipayCashCollService");
            //执行现金红包分发归集
            JSONObject retObj = cashCollInterface.collRedEnvelope(payOrder);
            String result = retObj.getString("result");
            String retMsg = retObj.getString("msg");
            boolean isSuccess = "success".equals(result);
            startTime = System.currentTimeMillis();
            JSONArray records = retObj.getJSONArray("records");
            if (records != null && records.size() > 0) {
                for (Object item : records) {
                    PayOrderCashCollRecord record = JSON.toJavaObject(((JSONObject) item), PayOrderCashCollRecord.class);
                    if (record.getStatus() == 0) {

                    }
                }
            }

        } catch (Exception e) {
            _log.info("处理现金红包分发任务错误 message={}，错误信息={}", message, getExceptionInfo(e));
            String errorMsg = "处理现金红包分发任务错误 message=" + message + "，错误信息=" + getExceptionInfo(e);
            TelegramUtil.SendMsg(errorMsg);
            //消费失败
            return Action.ReconsumeLater;
        }

        _log.info("现金红包分发执行=>耗时：{}", System.currentTimeMillis() - startTime);
        System.out.println("Receive: " + message);
        return Action.CommitMessage;
    }


    public static String getExceptionInfo(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

    public void Send(String msg) {

        _log.info("发送现金红包分发MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.QUEUE_RED_ENVELOPE,
                // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                rocketMqConfig.getOrderTag(),
                // Message Body 可以是任何二进制形式的数据， MQ不做任何干预
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                msg.getBytes());
        // 设置代表消息的业务关键属性，请尽可能全局唯一
        // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发
        // 注意：不设置也不会影响消息正常收发
        message.setKey("ORDERID_100");
        // 发送消息，只要不抛异常就是成功
        try {
            SendResult sendResult = producerBean.send(message);
            assert sendResult != null;
            System.out.println(sendResult);
            _log.info("发送现金红包分发MQ成功消息:sendResult={}", sendResult);

        } catch (ONSClientException e) {
            e.printStackTrace();
            _log.info("发送M现金红包分发Q失败消息:msg={}", msg);
            //出现异常意味着发送失败，为了避免消息丢失，建议缓存该消息然后进行重试。
        }
    }

    /**
     * 将触发风控自动关闭的通道子账户保存至 redis缓存
     *
     * @param msg
     * @param delay
     */
    public void Send(String msg, long delay) {
        _log.info("发送现金红包分发MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.QUEUE_RED_ENVELOPE,
                // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                rocketMqConfig.getOrderTag(),
                // Message Body 可以是任何二进制形式的数据， MQ不做任何干预
                // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                msg.getBytes());
        // 设置代表消息的业务关键属性，请尽可能全局唯一
        // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发
        // 注意：不设置也不会影响消息正常收发
        message.setKey("ORDERID_100");
        // 发送消息，只要不抛异常就是成功
        // 延时消息，单位毫秒（ms），在指定延迟时间（当前时间之后）进行投递，例如消息在 3 秒后投递
        long delayTime = System.currentTimeMillis() + delay;
        // 设置消息需要被投递的时间
        message.setStartDeliverTime(delayTime);
        try {
            SendResult sendResult = producerBean.send(message);
            assert sendResult != null;
            _log.info("发送MQ成功消息:sendResult={}", sendResult);
        } catch (ONSClientException e) {
            e.printStackTrace();
            _log.info("发送MQ失败消息:msg={}", msg);
            //出现异常意味着发送失败，为了避免消息丢失，建议缓存该消息然后进行重试。
        }
    }
}
