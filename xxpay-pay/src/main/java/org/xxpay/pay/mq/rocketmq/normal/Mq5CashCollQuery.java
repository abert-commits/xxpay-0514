package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.channel.alipay.AlipayConfig;
import org.xxpay.pay.mq.rocketmq.RocketMqConfig;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Mq5CashCollQuery implements MessageListener {


    private static final MyLog _log = MyLog.getLog(Mq5CashCollQuery.class);

    @Autowired
    private RocketMqConfig rocketMqConfig;

    @Autowired
    private ProducerBean producerBean;

    @Autowired
    public RpcCommonService rpcCommonService;

    @Autowired
    public BaseNotify5MchPay baseNotify5MchPay;

//    @Autowired
//    public BaseNotify5MchPay baseNotify4MchPay;


    private final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOnpyVEOvANpRu8lqwhLLxM+oFCDA6DmiReD6v2rKwkSz/s+k88QdN4qaDgdT66PCQGIomPU7N0mN3tJXiWEo8mUxAUD7jyZkDbysyGot9JEbAtDdWrmIe3UE8MvDAW02ahBfm4ZrT8E6mt4Lzqp8aAQ0FqsFJkR1XbgpJ/ITDjtAgMBAAECgYEA15mK21GPbj19CjRX/p791ukkbtEzaPzUY35N/F3mnsheNx+osXRjo9rGkOJDbYudK3K66vV5FSWCgfpP8pjdNM7ycahKwFg952MdtNpty3zpyFDkcWeMjVBSso5wtyqvgILfr4qgS3aHMintLSwSQtUjt5DhulzSyX3Z5YsAI8ECQQD2an3sAsSz4z6RfGlIr5tJc5Mi300btwpROGNmUVxAwbqpqq0UeSOgJEzmoMburS7X/v82Dn4QIfGQc0FZk3mlAkEA8wLOsgUUfsJfp7zcdxwD135EDnYm9jRib2vk9DBGulKV1MkbPMF8Z/3yn9UySH8b8pm3y7o7Ur5iuFb84xtPqQJBANQufp9rAtWjJ40/A6mDDMQCsP+mKE9lHY0ycOT5yeY46vKN9NtcNEEBAPbWGnYKyftTp450jDh4AfnQRMVNJ8ECQQCZ8eRZGCjEqIQKcfVEK2YvpJiehLDn9YWKSlKPcunLbTfnxcLQeU5DXrfOEzQ4gvWEeWba085y+5L0bn7jrFCJAkACF7+rox6W+wkuTfrSp4JAf0brfWwJhTV9QhKAYOzPjHU3xQHPcLq8h9rUitBbo1k8gfP+CW0lve7OZf0ecJl5";
    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    @Override
    public Action consume(Message message, ConsumeContext context) {
        String msg = null;
        String logPrefix = "订单分账查询";
        _log.info("订单分账查询任务.message={}", message);
        try {
            msg = new String(message.getBody(), "UTF-8");
            if (msg.contains("Hello MQ")) {
                return Action.CommitMessage;
            }

            _log.info("订单分账查询任务.msg={}", message);
            JSONObject msgObj = JSON.parseObject(msg);
            String payOrderId = msgObj.getString("payOrderId");
            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());

            _log.info("{}开始查询支付宝通道订单,payOrderId={}", logPrefix, payOrder.getPayOrderId());

            AlipayConfig alipayConfig = new AlipayConfig(payPassageAccount.getParam());
            String payOrderJson = JSONObject.toJSONString(payOrder);
            byte[] data = payOrderJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            byte[] alipayConfigByte = payPassageAccount.getParam().getBytes();

            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String payOrderRsa = Base64Utils.encode(encodedData);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            _log.info("参数信息，payOrder=》{},alipayConfig=》{}", payOrderJson, payPassageAccount.getParam());

            Map map = new HashMap();
            map.put("payOrder", payOrderRsa);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/querymerchantSplit", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("status").equals("3")) {
                String sendMsgTG = MessageFormat.format("预警消息->支付宝商家分账延迟结算。子账户ID:{0}已自动关闭,请运营人员核查是否已关闭,如未关闭,请手动关闭通道。时间:{1}",
                        payOrder.getPassageAccountId(), DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                PayPassageAccount account = new PayPassageAccount();
                account.setId(payOrder.getPassageAccountId());
                account.setStatus(MchConstant.PUB_NO);

                PayOrderCashCollRecord record = new PayOrderCashCollRecord();
                record.setPayOrderId(payOrderId);
                record.setStatus(MchConstant.PUB_LATE);
                record.setRemark("分账成功，延迟到账");
                rpcCommonService.rpcPayPassageAccountService.update(account);

                TelegramUtil.SendMsg(sendMsgTG);

                rpcCommonService.rpcPayOrderCashCollRecordService.update(record);
            }

            _log.info("订单分账查询 mq执行完成");
            if (resObj.getString("status").equals("2") && payOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS) {
                //来这里 表示订单分账成功，且订单状态为支付成功
                baseNotify5MchPay.doNotify(payOrder, true);
            }

            return Action.CommitMessage;
        } catch (Exception e) {
            //消费失败
            _log.info("订单分账查询 mq执行失败");

            return Action.ReconsumeLater;
        }
    }


    public void Send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.CASH_COLL_QUEUE_QUERY_NAME,
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
        _log.info("发送分账订单查询MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.CASH_COLL_QUEUE_QUERY_NAME,
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
