package org.xxpay.pay.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.ScheduledMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.CashCollInterface;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import javax.jms.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

@Component
public class Mq4CashColl {

    @Autowired
    private Queue cashCollQueue;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private RedisTemplate redisTemplate;


    private static final MyLog _log = MyLog.getLog(Mq4CashColl.class);

    public void send(String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        this.jmsTemplate.convertAndSend(this.cashCollQueue, msg);
    }

    /**
     * 发送延迟消息
     *
     * @param msg
     * @param delay
     */
    public void send(String msg, long delay) {
        _log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(this.cashCollQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1 * 1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
    }

    @JmsListener(destination = MqConfig.CASH_COLL_QUEUE_NAME)
    @Async("mqExecutor")
    public void receive(String msg) {
        _log.info("处理资金归集任务.msg={}", msg);
        long startTime = System.currentTimeMillis();
        JSONObject msgObj = JSON.parseObject(msg);
        String payOrderId = msgObj.getString("payOrderId");
        try {
            PayOrderCashCollRecord selectCondition = new PayOrderCashCollRecord();
            selectCondition.setPayOrderId(payOrderId);
            int row = rpcCommonService.rpcPayOrderCashCollRecordService.count(selectCondition, null, null);
            if (row > 0) {
                _log.info("处理资金归集任务已处理，本次结束.msg={}", msg);
                return;
            }

            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            String channelId = payOrder.getChannelId();
//        if(!"alipay_qr_pc".equals(channelId) && !"alipay_qr_h5".equals(channelId)){
//            _log.info("当前订单不属于支付宝当面付产品订单，本次结束.msg={}", msg);
//            return ;
//        }

            CashCollInterface cashCollInterface = (CashCollInterface) SpringUtil.getBean("alipayCashCollService");

            //执行资金归集
            JSONObject retObj = null;

            retObj = cashCollInterface.coll(payOrder);

            _log.info("资金归集渠道返回结果：" + retObj + ",耗时:{}", System.currentTimeMillis() - startTime);

            String result = retObj.getString("result");
            String retMsg = retObj.getString("msg");
            boolean isSuccess = "success".equals(result);

            startTime = System.currentTimeMillis();
            JSONArray records = retObj.getJSONArray("records");
            String payPassageAccountId = "";
            if (records != null) {
                for (Object item : records) {

                    PayOrderCashCollRecord record = JSON.toJavaObject(((JSONObject) item), PayOrderCashCollRecord.class);
                    payPassageAccountId = record.getPassageAccountId();
                    if (record.getType() == 0) {
                        record.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
                        record.setRemark(retMsg);
                    }

                    if (record.getType() == 1) {
                        if (record.getStatus() == MchConstant.PUB_NO) {
                            retObj.put("closeAccount", record.getPassageAccountId());
                            PayPassageAccount account = new PayPassageAccount();
                            account.setId(Integer.parseInt(record.getPassageAccountId()));
                            account.setStatus(MchConstant.PUB_NO);
                            rpcCommonService.rpcPayPassageAccountService.update(account);
                            String sendMsg = MessageFormat.format("预警消息->支付宝单笔转账失败,转账金额:{0},子账户ID:{1}。时间:{2}",
                                    AmountUtil.convertCent2Dollar(String.valueOf(record.getTransInAmount())),
                                    record.getPassageAccountId(),
                                    DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                            for (int i = 0; i < 3; i++) {
                                TelegramUtil.SendMsg(sendMsg);
                            }
                        }
                    }

                    rpcCommonService.rpcPayOrderCashCollRecordService.add(record);
                }
            }

            _log.info("解析资金归集渠道返回结果=>耗时：{}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();
            //是否关闭账号
            Integer closeAccount = retObj.getInteger("closeAccount");
            if (closeAccount != null) {
                int count = 0;
                Object value = redisTemplate.opsForValue().get(closeAccount);
                if (value == null) {
                    count++;
                    _log.info("分账失败，子账户ID{}。失败次数：1", closeAccount);
                    redisTemplate.opsForValue().set(closeAccount, 1);
                } else {
                    count = Integer.parseInt(value.toString());
                    count++;
                    _log.info("分账失败，子账户ID{}。失败次数：" + count, closeAccount);
                    redisTemplate.opsForValue().set(closeAccount, count);
                }

                String sendMsg = MessageFormat.format("预警消息->支付宝单笔分账失败,失败次数:{0},子账户ID:{1}已自动关闭,请运营人员核查是否已关闭,如未关闭,请手动关闭通道。时间:{2}",
                        count, String.valueOf(closeAccount), DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                if (count > 3) {
                    //累加分账失败次数
                    _log.info("cashCollCloseAccount is true ,分账失败,失败次数:" + count + "  关闭通道子账户信息 accountId = {}", closeAccount);
                    PayPassageAccount account = new PayPassageAccount();
                    account.setId(closeAccount);
                    account.setStatus(MchConstant.PUB_NO);
                    rpcCommonService.rpcPayPassageAccountService.update(account);
                    //清零失败次数
                    redisTemplate.delete(closeAccount);
                    TelegramUtil.SendMsg(sendMsg);
                }
            } else {
                //清零失败次数
                redisTemplate.delete(Integer.parseInt(payPassageAccountId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        _log.info("风控预警消息提示执行=>耗时：{}", System.currentTimeMillis() - startTime);
    }



    public void collExecutor(PayOrder payOrder) {
        _log.info("处理资金归集任务.msg={}", payOrder.getPayOrderId());
        long startTime = System.currentTimeMillis();
        try {

            CashCollInterface cashCollInterface = (CashCollInterface) SpringUtil.getBean("alipayCashCollService");

            JSONObject retObj = null;
            retObj = cashCollInterface.coll(payOrder);    //执行资金归集
            _log.info("资金归集渠道返回结果：" + retObj + ",耗时:{}", System.currentTimeMillis() - startTime);
            String result = retObj.getString("result");
            String retMsg = retObj.getString("msg");
            boolean isSuccess = "success".equals(result);

            startTime = System.currentTimeMillis();
            JSONArray records = retObj.getJSONArray("records");
            String payPassageAccountId = "";
            if (records != null) {
                for (Object item : records) {

                    PayOrderCashCollRecord record = JSON.toJavaObject(((JSONObject) item), PayOrderCashCollRecord.class);
                    payPassageAccountId = record.getPassageAccountId();
                    if (record.getType() == 0) {
                        record.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
                        record.setRemark(retMsg);
                    }

                    if (record.getType() == 1) {
                        if (record.getStatus() == MchConstant.PUB_NO) {
                            retObj.put("closeAccount", record.getPassageAccountId());
                            PayPassageAccount account = new PayPassageAccount();
                            account.setId(Integer.parseInt(record.getPassageAccountId()));
                            account.setStatus(MchConstant.PUB_NO);
                            rpcCommonService.rpcPayPassageAccountService.update(account);
                            String sendMsg = MessageFormat.format("预警消息->支付宝单笔转账失败,转账金额:{0},子账户ID:{1}。时间:{2}",
                                    AmountUtil.convertCent2Dollar(String.valueOf(record.getTransInAmount())),
                                    record.getPassageAccountId(),
                                    DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                            for (int i = 0; i < 3; i++) {
                                TelegramUtil.SendMsg(sendMsg);
                            }
                        }
                    }

                    rpcCommonService.rpcPayOrderCashCollRecordService.add(record);
                }
            }

            _log.info("解析资金归集渠道返回结果=>耗时：{}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();
            //是否关闭账号
            Integer closeAccount = retObj.getInteger("closeAccount");
            if (closeAccount != null) {
                int count = 0;
                Object value = redisTemplate.opsForValue().get(closeAccount);
                if (value == null) {
                    count++;
                    _log.info("分账失败，子账户ID{}。失败次数：1", closeAccount);
                    redisTemplate.opsForValue().set(closeAccount, 1);
                } else {
                    count = Integer.parseInt(value.toString());
                    count++;
                    _log.info("分账失败，子账户ID{}。失败次数：" + count, closeAccount);
                    redisTemplate.opsForValue().set(closeAccount, count);
                }

                String sendMsg = MessageFormat.format("预警消息->支付宝单笔分账失败,失败次数:{0},子账户ID:{1}已自动关闭,请运营人员核查是否已关闭,如未关闭,请手动关闭通道。时间:{2}",
                        count, String.valueOf(closeAccount), DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                if (count > 3) {
                    //累加分账失败次数
                    _log.info("cashCollCloseAccount is true ,分账失败,失败次数:" + count + "  关闭通道子账户信息 accountId = {}", closeAccount);
                    PayPassageAccount account = new PayPassageAccount();
                    account.setId(closeAccount);
                    account.setStatus(MchConstant.PUB_NO);
                    rpcCommonService.rpcPayPassageAccountService.update(account);
                    //清零失败次数
                    redisTemplate.delete(closeAccount);
                    TelegramUtil.SendMsg(sendMsg);
                }
            } else {
                //清零失败次数
                redisTemplate.delete(Integer.parseInt(payPassageAccountId));
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        _log.info("风控预警消息提示执行=>耗时：{}", System.currentTimeMillis() - startTime);
    }
}
