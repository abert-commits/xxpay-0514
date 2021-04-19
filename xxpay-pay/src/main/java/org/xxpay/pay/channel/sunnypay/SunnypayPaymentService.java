package org.xxpay.pay.channel.sunnypay;

//import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;

@Service
public class SunnypayPaymentService extends BasePayment {

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

  /*  @Override
    public JSONObject pay(PayOrder payOrder) {
        String channlId = payOrder.getChannelId();

        return pay(payOrder);
    }*/
}
