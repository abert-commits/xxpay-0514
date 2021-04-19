package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.util.AmountUtil;
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

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component

public class Mq5CashColl implements MessageListener {
    private static final MyLog _log = MyLog.getLog(Mq5CashColl.class);
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
        _log.info("处理资金归集任务.message={}", message);
        String msg = null;
        long startTime = System.currentTimeMillis();

        try {
            msg = new String(message.getBody(), "UTF-8");
            _log.info("处理资金归集任务.msg={}", message);
            JSONObject msgObj = JSON.parseObject(msg);
            String payOrderId = msgObj.getString("payOrderId");


            PayOrderCashCollRecord selectCondition = new PayOrderCashCollRecord();
            selectCondition.setPayOrderId(payOrderId);
            selectCondition.setStatus((byte) 1);
            int row = rpcCommonService.rpcPayOrderCashCollRecordService.count(selectCondition, null, null);
            if (row > 0) {
                _log.info("处理资金归集任务已处理，本次结束.msg={}", msg);
                return Action.CommitMessage;
            }
            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            CashCollInterface cashCollInterface = (CashCollInterface) SpringUtil.getBean("alipayCashCollService");

            //执行资金归集
            JSONObject retObj = cashCollInterface.coll(payOrder);
            _log.info("资金归集渠道返回结果：" + retObj + ",耗时:{}", System.currentTimeMillis() - startTime);

            String result = retObj.getString("result");
            String retMsg = retObj.getString("msg");
            boolean isSuccess = "success".equals(result);
            startTime = System.currentTimeMillis();
            JSONArray records = retObj.getJSONArray("records");
            String payPassageAccountId = "";
            List<PayOrderCashCollRecord> addList = new LinkedList<>();
            int fenzhangCount = 0;
            int zzFailCount = 0;//当前连续失败次数
            int zzFailCountByNow = 0;//当前转账失败次数
            if (records != null && records.size() > 0) {
                for (Object item : records) {
                    PayOrderCashCollRecord record = JSON.toJavaObject(((JSONObject) item), PayOrderCashCollRecord.class);
                    payPassageAccountId = record.getPassageAccountId();
                    if (record.getType() == 0) {
                        fenzhangCount++;
                        record.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
                        record.setRemark(isSuccess ? "成功" : retMsg);
                        if (record.getRemark().contains("ACQ.TRADE_SETTLE_ERROR")) {
                            record.setRemark("分账失败:订单金额未同步，请稍后再试！");
                        } else if (record.getRemark().contains("aop.ACQ.SYSTEM_ERROR")) {
                            record.setRemark("分账失败:支付宝返回系统异常,请联系技术或码商核查主号。");
                        }
                    }

                    if (record.getType() == 1) {
                        if (record.getStatus() == MchConstant.PUB_NO)
                        {
                            zzFailCountByNow++;
                            Object failCount = redisTemplate.opsForValue().get(payPassageAccountId);
                            if (failCount == null) {
                                redisTemplate.opsForValue().set(payPassageAccountId, 1);
                            } else {

                                zzFailCount = Integer.parseInt(failCount.toString());
                                zzFailCount++;
                                redisTemplate.opsForValue().set(payPassageAccountId, zzFailCount);
                            }
                        }else {
                            //如果没有连续失败，失败次数清0
                            //转账失败次数清零
                            redisTemplate.delete(payPassageAccountId);
                        }
                    }

                    addList.add(record);
                }

                List<PayOrderCashCollRecord> listCashColls = rpcCommonService.rpcPayOrderCashCollRecordService.selectByOrderId(payOrderId);
                if (listCashColls != null && listCashColls.size() > 0) {
                    rpcCommonService.rpcPayOrderCashCollRecordService.delete(payOrderId);
                }

                for (PayOrderCashCollRecord addItem : addList) {
                    rpcCommonService.rpcPayOrderCashCollRecordService.add(addItem);
                }

            } else {
                String sendMsg = MessageFormat.format("预警消息->支付订单:{0},处理资金归集失败。请运营人员根据订单号查询该订单在资金归集列表是否存在。如果不存在，请联系技术！时间:{1}",
                        payOrderId, DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS), retMsg);
                TelegramUtil.SendMsg(sendMsg);
                return Action.ReconsumeLater;
            }

            //如果当前转账失败次数等于0表示资金归集处理都成功 调用通知下游商户消息队列
            if (zzFailCountByNow == 0 && addList.get(0).getType() == 1) {
                _log.info("订单号：{}，资金归集处理成功=>单笔转账 调用通知下游商户消息队列。", payOrderId);
                baseNotify5MchPay.doNotify(payOrder, true);
            }

            if (zzFailCount > 3) {
                PayPassageAccount account = new PayPassageAccount();
                account.setId(new Integer(payPassageAccountId));
                account.setStatus(MchConstant.PUB_NO);
                rpcCommonService.rpcPayPassageAccountService.update(account);
                String sendMsg = MessageFormat.format("预警消息->支付宝单笔转账失败,失败次数:{0} ,子账户ID:{1}。时间:{2}",
                        zzFailCount,
                        payPassageAccountId,
                        DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
                TelegramUtil.SendMsg(sendMsg);
                redisAdd(Long.parseLong(payPassageAccountId));
                //转账失败次数清零
                redisTemplate.delete(payPassageAccountId);
            }


            if (isSuccess && fenzhangCount > 0) {
                JSONObject json = new JSONObject();
                json.put("payOrderId", payOrderId);
                //分账结算状态查询
                mq5CashCollQuery.Send(json.toJSONString(), 1500);
            } else if (fenzhangCount > 0) {

                //分账失败，再次发起分账请求
                _log.info("订单号：{}，分账失败，再次发起分账请求", payOrderId);
                Object value = redisTemplate.opsForValue().get(payOrder.getPayOrderId());
                int count = 0;
                if (value == null) {
                    redisTemplate.opsForValue().set(payOrder.getPayOrderId(), 1, 10, TimeUnit.MINUTES);
                } else {
                    count = Integer.parseInt(String.valueOf(value));
                    count++;
                    redisTemplate.opsForValue().set(payOrder.getPayOrderId(), count, 10, TimeUnit.MINUTES);
                }

                if (count < 4) {
                    //分账失败，再次发送消息队列
                    JSONObject json = new JSONObject();
                    json.put("payOrderId", payOrder.getPayOrderId());
                    this.Send(json.toJSONString(), 5000);
                } else {
                    //大于等于3次，就停止自动发送分账请求
                    redisTemplate.delete(payOrder.getPayOrderId());
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

                String sendMsg = MessageFormat.format("预警消息->支付宝单笔分账失败,失败次数:{0},子账户ID:{1}已自动关闭,请运营人员核查是否已关闭,如未关闭,请手动关闭通道。时间:{2}。失败原因：{3}",
                        count, String.valueOf(closeAccount), DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS), retMsg);
                if (count > 3) {
                    //累加分账失败次数
                    _log.info("cashCollCloseAccount is true ,分账失败,失败次数:" + count + "  关闭通道子账户信息 accountId = {}", closeAccount);
                    PayPassageAccount account = new PayPassageAccount();
                    account.setId(closeAccount);
                    account.setStatus(MchConstant.PUB_NO);
                    rpcCommonService.rpcPayPassageAccountService.update(account);
                    redisAdd(new Long(closeAccount));
                    //清零失败次数
                    redisTemplate.delete(closeAccount);
                    TelegramUtil.SendMsg(sendMsg);

                }
            } else {
                //清零失败次数
                redisTemplate.delete(Integer.parseInt(payPassageAccountId));
            }


        } catch (Exception e) {
            _log.info("处理资金归集任务错误 message={}，错误信息={}", message, getExceptionInfo(e));
            String errorMsg = "处理资金归集任务错误 message=" + message + "，错误信息=" + getExceptionInfo(e);
            TelegramUtil.SendMsg(errorMsg);

            //消费失败
            return Action.ReconsumeLater;
        }

        _log.info("风控预警消息提示执行=>耗时：{}", System.currentTimeMillis() - startTime);
        System.out.println("Receive: " + message);
        return Action.CommitMessage;
    }


    public static String getExceptionInfo(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }
   /* @PostConstruct
    public void testSend() {
        //循环发送消息
        for (int i = 0; i < 100; i++) {
            Message msg = new Message( //
                    // Message所属的Topic
                    RocketMqConfig.CASH_COLL_QUEUE_NAME,
                    // Message Tag 可理解为Gmail中的标签，对消息进行再归类，方便Consumer指定过滤条件在MQ服务器过滤
                    rocketMqConfig.getOrderTag(),
                    // Message Body 可以是任何二进制形式的数据， MQ不做任何干预
                    // 需要Producer与Consumer协商好一致的序列化和反序列化方式
                    ("Hello MQ====="+i).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一
            // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发
            // 注意：不设置也不会影响消息正常收发
             msg.setKey("ORDERID_100");
            // 发送消息，只要不抛异常就是成功
            try {
                SendResult sendResult = producerBean.send(msg);
                assert sendResult != null;
                System.out.println(sendResult);
            } catch (ONSClientException e) {
                System.out.println("发送失败");
                //出现异常意味着发送失败，为了避免消息丢失，建议缓存该消息然后进行重试。
            }
        }
    }*/

    public void Send(String msg) {

        _log.info("再次发起分账:" + msg);
        _log.info("发送MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.CASH_COLL_QUEUE_NAME,
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
     * 将触发风控自动关闭的通道子账户保存至 redis缓存
     *
     * @param msg
     * @param delay
     */
    public void Send(String msg, long delay) {
        _log.info("发送MQ消息:msg={}", msg);
        Message message = new Message( //
                // Message所属的Topic
                RocketMqConfig.CASH_COLL_QUEUE_NAME,
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


    private void redisAdd(Long passageAccountId) {
        try {
            String key = "passageAccountIdClose";
            List<Long> list = (List<Long>) redisTemplate.opsForValue().get(key);
            if (list == null) {
                list = new LinkedList<>();
            }

            List<Long> data = list.stream().filter(a -> a.longValue() == passageAccountId.longValue()).collect(Collectors.toList());
            if (data == null || data.size() == 0) {
                list.add(passageAccountId);
            }

            redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);

        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info("将触发风控自动关闭的通道子账户保存至 redis缓存", ex.getStackTrace() + ex.getMessage());
        }
    }
}
