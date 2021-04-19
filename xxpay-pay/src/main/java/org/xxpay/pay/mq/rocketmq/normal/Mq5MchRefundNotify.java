package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.pay.mq.Mq4MchAgentpayNotify;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;

@Component
public class Mq5MchRefundNotify implements MessageListener {


    private static final MyLog _log = MyLog.getLog(Mq5MchRefundNotify.class);

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private ProducerBean producerBean;

    @Autowired
    public RpcCommonService rpcCommonService;

    @Override
    public Action consume(Message message, ConsumeContext context) {
        String msg = null;
        _log.info("商户退款通知.message={}", message);
        try {
            msg = new String(message.getBody(), "UTF-8");
            String logPrefix = "【商户退款通知】";
            _log.info("{}接收消息:msg={}", logPrefix, msg);
            JSONObject msgObj = JSON.parseObject(msg);
            String respUrl = msgObj.getString("url");
            String orderId = msgObj.getString("orderId");
            int count = msgObj.getInteger("count");
            if(StringUtils.isEmpty(respUrl)) {
                _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
                return Action.CommitMessage;
            }
            String httpResult = "";
            try{
                httpResult = XXPayUtil.call4Post(respUrl);
            }catch (Exception e) {
                _log.error(e, "发起通知请求异常");
            }
            int cnt = count + 1;
            _log.info("{}notifyCount={}", logPrefix, cnt);
            if("success".equalsIgnoreCase(httpResult)){
                // 修改支付订单表
                try {
                    int result = rpcCommonService.rpcRefundOrderService.updateStatus4Complete(orderId);
                    _log.info("{}修改payOrderId={},订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error(e, "修改订单状态为处理完成异常");
                }
                // 修改通知
                try {
                    int result = rpcCommonService.rpcMchNotifyService.updateMchNotifySuccess(orderId, httpResult, (byte) cnt);
                    _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
                }catch (Exception e) {
                    _log.error(e, "修改商户支付通知异常");
                }
                return Action.CommitMessage;// 通知成功结束
            }else {
                // 修改通知次数
                try {
                    int result = rpcCommonService.rpcMchNotifyService.updateMchNotifyFail(orderId, httpResult, (byte) cnt);
                    _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
                }catch (Exception e) {
                    _log.error(e, "修改商户支付通知异常");
                }
                if (cnt > 5) {
                    _log.info("{}orderId={},通知次数notifyCount({})>5,停止通知", logPrefix, orderId, cnt);
                    return Action.CommitMessage;
                }
                // 通知失败，延时再通知
                msgObj.put("count", cnt);
                this.Send(msgObj.toJSONString(), cnt * 60 * 1000);
                _log.info("{}orderId={},发送延时通知完成,通知次数:{},{}秒后执行通知", logPrefix, orderId, cnt, cnt * 60);
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
                RocketMqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME,
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
                RocketMqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME,
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
