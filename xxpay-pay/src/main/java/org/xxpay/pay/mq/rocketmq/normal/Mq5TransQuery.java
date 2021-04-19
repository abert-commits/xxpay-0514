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
import org.xxpay.core.entity.TransOrder;
import org.xxpay.pay.channel.TransInterface;
import org.xxpay.pay.mq.BaseNotify4MchTrans;
import org.xxpay.pay.mq.Mq4MchAgentpayNotify;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

@Component
public class Mq5TransQuery implements MessageListener {


    private static final MyLog _log = MyLog.getLog(Mq5TransQuery.class);

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private ProducerBean producerBean;

    @Autowired
    public RpcCommonService rpcCommonService;


    @Autowired
    public BaseNotify5MchTrans baseNotify5MchTrans;
    @Override
    public Action consume(Message message, ConsumeContext context) {
        String msg = null;
        _log.info("商户转账通知.message={}", message);
        try {
            msg = new String(message.getBody(), "UTF-8");
            _log.info("处理转账订单查询任务.msg={}", msg);
            JSONObject msgObj = JSON.parseObject(msg);
            int count = msgObj.getIntValue("count");
            String transOrderId = msgObj.getString("transOrderId");
            String channelName = msgObj.getString("channelName");
            TransOrder transOrder = rpcCommonService.rpcTransOrderService.findByTransOrderId(transOrderId);
            if(transOrder == null) {
                _log.warn("查询转账订单为空,transOrderId={}", transOrderId);
                return Action.CommitMessage;
            }
            if(transOrder.getStatus() != PayConstant.TRANS_STATUS_TRANING) {
                _log.warn("订单状态不是转账中({}),不需查询上游渠道.transOrderId={}", PayConstant.TRANS_STATUS_TRANING, transOrderId);
                return Action.CommitMessage;
            }

            TransInterface transInterface = (TransInterface) SpringUtil.getBean(channelName.toLowerCase() +  "TransService");
            JSONObject retObj = transInterface.query(transOrder);
            // 查询成功
            if(XXPayUtil.isSuccess(retObj)) {
                // 1-处理中 2-成功 3-失败
                int status = retObj.getInteger("status");
                if(status == 2) {
                    String channelOrderNo = retObj.getString("channelOrderNo");
                    int updateTransOrderRows = rpcCommonService.rpcTransOrderService.updateStatus4Success(transOrderId, channelOrderNo);
                    _log.info("更新转账订单状态为成功({}),transOrderId={},返回结果:{}", PayConstant.TRANS_STATUS_SUCCESS, transOrderId, updateTransOrderRows);
                    if (updateTransOrderRows == 1) {
                        // 通知业务系统
                        baseNotify5MchTrans.doNotify(transOrder, true);
                        return Action.CommitMessage;
                    }
                }else if(status == 3) {
                    String channelOrderNo = retObj.getString("channelOrderNo");
                    String channelErrCode = retObj.getString("channelErrCode");
                    String channelErrMsg = retObj.getString("channelErrMsg");
                    int updateTransOrderRows = rpcCommonService.rpcTransOrderService.updateStatus4Fail(transOrderId, channelErrCode, channelErrMsg, channelOrderNo);
                    _log.info("更新转账订单状态为失败({}),transOrderId={},返回结果:{}", PayConstant.TRANS_STATUS_FAIL, transOrderId, updateTransOrderRows);
                    if (updateTransOrderRows == 1) {
                        // 通知业务系统
                        baseNotify5MchTrans.doNotify(transOrder, true);
                        return Action.CommitMessage;
                    }
                }
            }

            // 发送延迟消息,继续查询
            int cnt = count + 1;
            if(cnt <= 10) {
                // 通知频率为15/15/30/180/1800/1800/1800/1800/3600/36000
                // 通知失败，延时再通知
                msgObj.put("count", cnt);
                long delay = getNotifyDelay(cnt);
                _log.info("[转账查询]{}次: msg={}", cnt, msgObj);
                this.Send(msgObj.toJSONString(), delay * 1000);
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
                RocketMqConfig.TRANS_QUERY_QUEUE_NAME,
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
                RocketMqConfig.TRANS_QUERY_QUEUE_NAME,
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

    /**
     * 获取延迟秒数
     * @param cnt
     * @return
     */
    long getNotifyDelay(int cnt) {
        switch (cnt) {
            case 1:
                return 30;
            case 2:
                return 60;
            case 3:
                return 120;
            case 4:
                return 180;
            case 5:
                return 1800;
            case 6:
                return 1800;
            case 7:
                return 1800;
            case 8:
                return 1800;
            case 9:
                return 3600;
            case 10:
                return 36000;
            default:
                return 180;
        }
    }

}
