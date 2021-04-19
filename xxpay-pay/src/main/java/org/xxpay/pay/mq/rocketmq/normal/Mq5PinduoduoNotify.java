package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PDDUtils;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PinduoduoOrders;
import org.xxpay.pay.mq.Mq4MchAgentpayNotify;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;

@Component
public class Mq5PinduoduoNotify implements MessageListener {


    private static final MyLog _log = MyLog.getLog(Mq5PinduoduoNotify.class);

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
        _log.info("拼多多订单状态查询.message={}", message);
        try {
            msg = new String(message.getBody(), "UTF-8");
            long startTime = System.currentTimeMillis();
            String logPrefix = "【拼多多订单状态查询】";
            _log.info("{}接收消息:msg={}", logPrefix, msg);
            JSONObject msgObj = JSON.parseObject(msg);
            String orderSn = msgObj.getString("order_sn");
            String payOrderId = msgObj.getString("payOrderId");
            String PDDAccessToken = msgObj.getString("PDDAccessToken");
            String nginxIP = msgObj.getString("nginxIP");
            String nginxPort = msgObj.getString("nginxPort");
            int count = msgObj.getInteger("count");
            //请求拼多多查看订单状态
            String httpResult = "";
            JSONObject jsonObject = null;
            try {
                httpResult = PDDUtils.orderDetialUrl(PDDAccessToken, nginxIP, nginxPort, orderSn);
                String html1 = httpResult.substring(httpResult.indexOf("window.rawData=")).replace(" ", "");
                if (html1.contains("window.rawData= null") || html1.contains("window.rawData=null")) {
                    _log.info("{} 拼多多查询订单状态 查询失败 订单=={}", orderSn);
                    return Action.CommitMessage;
                }
                String html2 = html1.substring(html1.indexOf("{"), html1.indexOf("}};") + 2);
                jsonObject = JSONObject.parseObject(html2).getJSONObject("data");
            } catch (Exception e) {
                _log.error(e, "发起通知请求异常");
            }
            //查看当前订单 状态 如果是待发货  修改订单状态

            int cnt = count + 1;
            _log.info("{}orderSn={},jsonObject={},请求耗时:{} ms", logPrefix, orderSn, jsonObject, System.currentTimeMillis() - startTime);
            Integer status = getStatus(jsonObject.getString("chatStatusPrompt"));
            if (status == 1) {
                // 修改支付订单表
                try {
                    //修改拼多多订单表
                    PinduoduoOrders pinduoduoOrders = new PinduoduoOrders();
                    pinduoduoOrders.setStatus(status);
                    int result = rpcCommonService.rpcIPinduoduoOrderService.updateStatus(orderSn, pinduoduoOrders);
                    _log.info("{}orderSn={},订单状态为待发货->{}", logPrefix, orderSn, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error(e, "修改订单状态为处理完成异常");
                    return Action.CommitMessage;
                }
                int result = 0;
                PayOrder order = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
                // 修改通知
                try {
                    //修改订单表 并且回调
                    result = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrderId);
                    _log.error("{}更新四方支付状态成功,将payOrderId={},更新payStatus={}", logPrefix, payOrderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}", logPrefix, payOrderId, "失败");
                }
                if (result > 0) {
                    // result
                    baseNotify5MchPay.doNotify(order, true);
                }
                return Action.CommitMessage; // 通知成功结束
            } else {
                if (cnt > 5) {
                    _log.info("{}orderSn={},通知次数notifyCount({})>5,停止通知", logPrefix, orderSn, cnt);
                    return Action.CommitMessage;
                }
                // 通知失败，延时再通知
                msgObj.put("count", cnt);
                this.Send(msgObj.toJSONString(), cnt * 60 * 1000);
                _log.info("{}orderSn={},发送延时通知完成,通知次数:{},{}秒后执行通知", logPrefix, orderSn, cnt, cnt * 60);
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
                RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME_PDD,
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
                RocketMqConfig.MCH_PAY_NOTIFY_QUEUE_NAME_PDD,
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

    public int getStatus(String status) {
        if (status.contains("待付款")) {
            return 0;
        } else if (status.contains("待发货")) {
            return 1;

        } else if (status.contains("待收货")) {
            return 2;

        } else if (status.contains("待评价")) {
            return 3;

        } else if (status.contains("交易已取消")) {
            return 4;

        } else {
            return 0;
        }
    }
}
