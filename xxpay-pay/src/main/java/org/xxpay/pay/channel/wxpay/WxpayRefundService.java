package org.xxpay.pay.channel.wxpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.RefundOrder;
import org.xxpay.pay.channel.BaseRefund;

import java.io.File;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/25
 * @description:
 */
@Service
public class WxpayRefundService extends BaseRefund {

    private static final MyLog _log = MyLog.getLog(WxpayRefundService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }


    public JSONObject refund(RefundOrder refundOrder) {
        String logPrefix = "【微信退款】";
        JSONObject retObj = buildRetObj();
        try{
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(getRefundParam(refundOrder), "", payConfig.getCertRootPath() + File.separator + getChannelName(), payConfig.getNotifyUrl(getChannelName()));
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            WxPayRefundRequest wxPayRefundRequest = buildWxPayRefundRequest(refundOrder, wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            WxPayRefundResult result;
            try {
                result = wxPayService.refund(wxPayRefundRequest);
                _log.info("{} >>> 下单成功", logPrefix);
                retObj.put("isSuccess", true);
                retObj.put("refundOrderId", refundOrderId);
                retObj.put("channelOrderNo", result.getRefundId());
            } catch (WxPayException e) {
                _log.error(e, "下单失败");
                //出现业务错误
                _log.info("{}下单返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                retObj.put("isSuccess", false);
                retObj.put("channelErrCode", e.getErrCode());
                retObj.put("channelErrMsg", e.getErrCodeDes());
            }
            return retObj;
        }catch (Exception e) {
            _log.error(e, "微信退款异常");
            retObj = buildFailRetObj();
            return retObj;
        }
    }

    public JSONObject query(RefundOrder refundOrder) {
        String logPrefix = "【微信退款查询】";
        JSONObject retObj = buildRetObj();
        try{
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(getRefundParam(refundOrder), "", payConfig.getCertRootPath() + File.separator + getChannelName(), payConfig.getNotifyUrl(getChannelName()));
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            WxPayRefundQueryResult result;
            try {
                result = wxPayService.refundQuery(refundOrder.getChannelPayOrderNo(), refundOrder.getPayOrderId(), refundOrder.getRefundOrderId(), refundOrder.getChannelOrderNo());
                _log.info("{} >>> 成功", logPrefix);
                retObj.putAll((Map) JSON.toJSON(result));
                retObj.put("isSuccess", true);
                retObj.put("refundOrderId", refundOrderId);
            } catch (WxPayException e) {
                _log.error(e, "失败");
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("err_code:{}", e.getErrCode());
                _log.info("err_code_des:{}", e.getErrCodeDes());
                retObj.put("channelErrCode", e.getErrCode());
                retObj.put("channelErrMsg", e.getErrCodeDes());
                retObj.put("isSuccess", false);
            }
            return retObj;
        }catch (Exception e) {
            _log.error(e, "微信退款查询异常");
            retObj = buildFailRetObj();
            return retObj;
        }
    }

    /**
     * 构建微信退款请求数据
     * @param refundOrder
     * @param wxPayConfig
     * @return
     */
    WxPayRefundRequest buildWxPayRefundRequest(RefundOrder refundOrder, WxPayConfig wxPayConfig) {
        // 微信退款请求对象
        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setTransactionId(refundOrder.getChannelPayOrderNo());
        request.setOutTradeNo(refundOrder.getPayOrderId());
        request.setDeviceInfo(refundOrder.getDevice());
        request.setOutRefundNo(refundOrder.getRefundOrderId());
        request.setRefundDesc(refundOrder.getRemarkInfo());
        request.setRefundFee(refundOrder.getRefundAmount().intValue());
        request.setRefundFeeType("CNY");
        request.setTotalFee(refundOrder.getPayAmount().intValue());
        return request;
    }

}
