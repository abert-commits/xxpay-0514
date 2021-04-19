package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.IPUtility;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.TelegramUtil;
import org.xxpay.core.entity.*;
import org.xxpay.pay.channel.dabaopay.DabaopayPaymentService;
import org.xxpay.pay.mq.BaseNotify4MchPay;
import org.xxpay.pay.mq.rocketmq.normal.BaseNotify5MchPay;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
@Component
public abstract class BasePayNotify extends BaseService implements PayNotifyInterface {

    private static final MyLog _log = MyLog.getLog(BasePayNotify.class);

    @Autowired
    public RpcCommonService rpcCommonService;

    @Autowired
    public PayConfig payConfig;

    @Autowired
    public BaseNotify5MchPay baseNotify4MchPay;

/*    @Autowired
    public BaseNotify4MchPay baseNotify4MchPay;*/

    public abstract String getChannelName();

    public JSONObject doNotify(Object notifyData) {
        return null;
    }

    public JSONObject doReturn(Object notifyData) {
        return new JSONObject();
    }

    /**
     * 获取三方支付配置信息
     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
     *
     * @param payOrder
     * @return
     */
    public String getPayParam(PayOrder payOrder) {
        String payParam = "";
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
        if (payPassageAccount != null && payPassageAccount.getStatus() == MchConstant.PUB_YES) {
            payParam = payPassageAccount.getParam();
        }
        if (StringUtils.isBlank(payParam)) {
            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
        }
        return payParam;
    }


    public SortedMap GetParamsToMap(HttpServletRequest request) {
        SortedMap<String, String> map = new TreeMap<>();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length > 0) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        return map;
    }

    public JSONObject GetParamsToJSONObject(HttpServletRequest request) {
        JSONObject map = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length > 0) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        return map;
    }

    public String GetInput(Object notifyData) {
        String result = "";
        if (notifyData instanceof String) {
            result = (String) notifyData;
        } else if (notifyData instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) notifyData;
            try {
                result = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            } catch (IOException e) {
                _log.error(e, "");
            }
        }

        return  result;
    }

    /**
     * 校验回调IP
     *
     * @param passageId
     * @return
     */
    public boolean CheckCallIP(HttpServletRequest request, Integer passageId, PayOrder payOrder) {
        try {
            PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(passageId);
            String requestIp = IPUtility.getRealIpAddress(request);
            _log.info("订单号:" + payOrder.getPayOrderId() + ",请求IP：" + requestIp);
            Integer count = 0;
            //判断通道是否设置了回调IP
            if (StringUtils.isNotBlank(payPassage.getCallbackIp())) {
                //如果配置了,取出通道配置的回调IP，循环比较当前请求的IP
                String ipStr = payPassage.getCallbackIp();
                String[] ips = ipStr.split("\\|");
                for (String item : ips) {
                    if (requestIp.equals(item)) {
                        count++;
                        break;
                    }
                }
            } else {
                //如果没有配置回调IP，默认检查通过
                return true;
            }

            if (count == 0) {
                String sendMsg = MessageFormat.format("回调异常,回调IP非白名单=>所属通道:{0},回调IP:{1},订单号:{2}", payOrder.getChannelId(), requestIp, payOrder.getPayOrderId());
                TelegramUtil.SendMsg(sendMsg);
            } else {
                _log.info("回调白名单校验成功,订单号:" + payOrder.getPayOrderId() + ",请求IP：" + requestIp);
            }

            return count > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
