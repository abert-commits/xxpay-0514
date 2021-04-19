package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransOrderQueryModel;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.TransOrder;
import org.xxpay.pay.channel.BaseTrans;

import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/25
 * @description:
 */
@Service
public class AlipayTransService extends BaseTrans {

    private static final MyLog _log = MyLog.getLog(AlipayTransService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    public JSONObject trans(TransOrder transOrder) {
        String logPrefix = "【支付宝转账】";
        String transOrderId = transOrder.getTransOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getTransParam(transOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        model.setOutBizNo(transOrderId);
        model.setPayeeType("ALIPAY_LOGONID");                            // 收款方账户类型
        model.setPayeeAccount(transOrder.getChannelUser());              // 收款方账户
        model.setAmount(AmountUtil.convertCent2Dollar(transOrder.getAmount().toString()));
        model.setPayerShowName("支付转账");
        model.setPayeeRealName(transOrder.getAccountName());
        model.setRemark(transOrder.getRemarkInfo());
        request.setBizModel(model);
        JSONObject retObj = buildRetObj();
        retObj.put("transOrderId", transOrderId);
        retObj.put("isSuccess", false);
        try {
            AlipayFundTransToaccountTransferResponse response = client.execute(request);
            if(response.isSuccess()) {
                retObj.put("isSuccess", true);
                retObj.put("channelOrderNo", response.getOrderId());
            }else {
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                retObj.put("channelErrCode", response.getSubCode());
                retObj.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj = buildFailRetObj();
        }
        return retObj;
    }

    public JSONObject query(TransOrder transOrder) {
        String logPrefix = "【支付宝转账查询】";
        String transOrderId = transOrder.getTransOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getTransParam(transOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
        model.setOutBizNo(transOrderId);
        model.setOrderId(transOrder.getChannelOrderNo());
        request.setBizModel(model);
        JSONObject retObj = buildRetObj();
        retObj.put("transOrderId", transOrderId);
        try {
            AlipayFundTransOrderQueryResponse response = client.execute(request);
            if(response.isSuccess()){
                retObj.putAll((Map) JSON.toJSON(response));
                retObj.put("isSuccess", true);
            }else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                retObj.put("channelErrCode", response.getSubCode());
                retObj.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj = buildFailRetObj();
        }
        return retObj;
    }

    @Override
    public JSONObject balance(String payParam) {
        return null;
    }


}
