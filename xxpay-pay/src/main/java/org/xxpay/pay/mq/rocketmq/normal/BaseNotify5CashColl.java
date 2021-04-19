package org.xxpay.pay.mq.rocketmq.normal;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.SysConfig;
import org.xxpay.pay.service.RpcCommonService;

import java.util.Random;

@Component
public class BaseNotify5CashColl {

    private static final MyLog _log = MyLog.getLog(BaseNotify5CashColl.class);
    @Autowired
    private Mq5CashColl mq5CashColl;


    /**
     * 处理资金结算
     */
    public void doNotify(String payOrderId) {
        try {
            int max=4000;
            int min=1;
            Random random = new Random();
            int s = random.nextInt(max)%(max-min+1) + min;
            JSONObject json = new JSONObject();
            json.put("payOrderId", payOrderId);
            mq5CashColl.Send(json.toString(), s);
        } catch (Exception e) {
            _log.error(e, "payOrderId={},sendMessage error.", payOrderId);
        }
    }

}
