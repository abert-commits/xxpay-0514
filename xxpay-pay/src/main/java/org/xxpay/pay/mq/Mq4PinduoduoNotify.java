package org.xxpay.pay.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PDDUtils;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PinduoduoOrders;

import javax.jms.Queue;

/**
 * pdd mq 查询订单是否完成
 */
@Component
public class Mq4PinduoduoNotify extends Mq4MchNotify {
    @Autowired
    private Queue payPddNotifyQueue;

    @Autowired
    public BaseNotify4MchPay baseNotify4MchPay;

    private static final MyLog _log = MyLog.getLog(Mq4PinduoduoNotify.class);

    public void send(String msg) {
        super.send(payPddNotifyQueue, msg);
    }

    @JmsListener(destination = MqConfig.MCH_PAY_NOTIFY_QUEUE_NAME_PDD)
    @Async("mqExecutor")
    public void receive(String msg) {
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
                return;
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
                return;
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
                baseNotify4MchPay.doNotify(order, true);
            }
            return; // 通知成功结束
        } else {
            if (cnt > 5) {
                _log.info("{}orderSn={},通知次数notifyCount({})>5,停止通知", logPrefix, orderSn, cnt);
                return;
            }
            // 通知失败，延时再通知
            msgObj.put("count", cnt);
            this.send(payPddNotifyQueue, msgObj.toJSONString(), cnt * 60 * 1000);
            _log.info("{}orderSn={},发送延时通知完成,通知次数:{},{}秒后执行通知", logPrefix, orderSn, cnt, cnt * 60);
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
