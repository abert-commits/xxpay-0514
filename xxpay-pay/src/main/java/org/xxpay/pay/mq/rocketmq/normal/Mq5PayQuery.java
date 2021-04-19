package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.mq.BaseNotify4MchPay;
import org.xxpay.pay.mq.Mq4MchAgentpayNotify;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

@Component
public class Mq5PayQuery implements MessageListener {


    private static final MyLog _log = MyLog.getLog(Mq5PayQuery.class);

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private ProducerBean producerBean;

    @Autowired
    public RpcCommonService rpcCommonService;

    @Autowired
    public BaseNotify5MchPay baseNotify5MchPay;

    @Override
    public Action consume(Message message, ConsumeContext context) {
        String msg = null;
        _log.info("支付订单查询任务.message={}", message);
        try {
            msg = new String(message.getBody(), "UTF-8");
            _log.info("处理支付订单查询任务.msg={}", msg);
            JSONObject msgObj = JSON.parseObject(msg);
            int count = msgObj.getIntValue("count");
            String payOrderId = msgObj.getString("payOrderId");
            String channelName = msgObj.getString("channelName");
            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            if (payOrder == null) {
                _log.warn("查询支付订单为空,payOrderId={}", payOrderId);
                return Action.CommitMessage;
            }
            if (payOrder.getStatus() != PayConstant.PAY_STATUS_PAYING) {
                _log.warn("订单状态不是支付中({}),不需查询渠道.payOrderId={}", PayConstant.PAY_STATUS_PAYING, payOrderId);
                return Action.CommitMessage;
            }

            PaymentInterface paymentInterface = (PaymentInterface) SpringUtil.getBean(channelName.toLowerCase() + "PaymentService");
            JSONObject retObj = paymentInterface.query(payOrder);
            _log.info("查询响应=>payOrderId={},result={}", payOrderId, retObj.toJSONString());
            // 订单为成功
            if (XXPayUtil.isSuccess(retObj) && "2".equals(retObj.get("status"))) {
                //String transaction_id = retObj.getString("transaction_id");
                int updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                _log.error("将payOrderId={}订单状态更新为支付成功,result={}", payOrderId, updatePayOrderRows);
                if (updatePayOrderRows == 1) {
                    // 通知业务系统
                    baseNotify5MchPay.doNotify(payOrder, true);
                    return Action.CommitMessage;
                }
            }

            // 发送延迟消息,继续查询
            if (count++ < 5) {
                msgObj.put("count", count);
                Send(msgObj.toJSONString(), 20 * 1000);   // 延迟5秒查询
            }
            return Action.CommitMessage;
        } catch (Exception e) {
            //消费失败
            return Action.ReconsumeLater;
        }
    }


    public void Send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.PAY_QUERY_QUEUE_NAME,
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
            _log.info("发送MQ成功消息:sendResult={}", sendResult);

        } catch (ONSClientException e) {
            e.printStackTrace();
            _log.info("发送MQ失败消息:msg={}", msg);
            //出现异常意味着发送失败，为了避免消息丢失，建议缓存该消息然后进行重试。
        }
    }

    /**
     * 发送延迟消息
     *
     * @param msg
     * @param delay
     */
    public void Send(String msg, long delay) {
        _log.info("发送MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.PAY_QUERY_QUEUE_NAME,
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
