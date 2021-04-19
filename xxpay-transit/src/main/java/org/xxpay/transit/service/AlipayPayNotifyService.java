package org.xxpay.transit.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayPayNotifyService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(AlipayPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    public String doNotify(Object notifyData) {
        // 构建返回对象
        JSONObject retObj = buildRetObj();
        try {
            String logPrefix = "【处理支付宝支付回调】";
            _log.info("====== 开始处理支付宝支付回调通知 ======");
            Map params = null;
            if (notifyData instanceof Map) {
                params = (HashMap) notifyData;
            } else if (notifyData instanceof HttpServletRequest) {
                params = buildNotifyData((HttpServletRequest) notifyData);
            }
            _log.info("{}请求数据:{}", logPrefix, params);
            if (params == null || params.isEmpty()) {
                _log.info("获取请求数据为空");
                return "fail";
            }

            //转发
            String sendMsg = XXPayUtil.mapToString(params).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(payConfig.getNotifyTransUrl(), sendMsg);
            return res;
        } catch (Exception ex) {
            _log.error("接受支付宝回调发生异常=》" + ex.getStackTrace() + ex.getMessage());
            return "fail";
        }
    }
}

