package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.OpenApiRoyaltyDetailInfoPojo;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.RefundOrder;
import org.xxpay.pay.channel.BaseRefund;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/25
 * @description:
 */
@Service
public class AlipayRefundService extends BaseRefund {

    private static final MyLog _log = MyLog.getLog(AlipayRefundService.class);

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ALIPAY;
    }

    public JSONObject refund(RefundOrder refundOrder) {
        String logPrefix = "【支付宝退款】";
        String refundOrderId = refundOrder.getRefundOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getRefundParamAll(refundOrder));

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl("https://openapi.alipay.com/gateway.do");  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
        certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
        certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
        certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
        certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8
        AlipayClient client = null;
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        model.setRefundAmount(AmountUtil.convertCent2Dollar(refundOrder.getRefundAmount().toString()));
        model.setRefundReason("正常退款");
        request.setBizModel(model);
        JSONObject retObj = buildRetObj();
        retObj.put("refundOrderId", refundOrderId);
        retObj.put("isSuccess", false);
        try {
            String appCertPublicKeyPath = GetAppCertPublicKey(refundOrderId, alipayConfig.getAppId());
            String alipayCertPublicKeyPath = GetAlipayCertPublicKey(refundOrderId, alipayConfig.getAppId());
            String alipayRootCertPath = GetAlipayRootCert(refundOrderId, alipayConfig.getAppId());
            certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
            certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
            certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
            certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
            client = new DefaultAlipayClient(certAlipayRequest);

            AlipayTradeRefundResponse response = client.certificateExecute(request);
            if (response.isSuccess()) {
                retObj.put("isSuccess", true);
                retObj.put("channelOrderNo", response.getTradeNo());
                retObj.put("channelErrMsg","退款成功");
            } else {
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


    public JSONObject query(RefundOrder refundOrder) {
        String logPrefix = "【支付宝退款查询】";
        String refundOrderId = refundOrder.getRefundOrderId();
        AlipayConfig alipayConfig = new AlipayConfig(getRefundParam(refundOrder));
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(), alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        request.setBizModel(model);
        JSONObject retObj = buildRetObj();
        retObj.put("refundOrderId", refundOrderId);
        try {
            AlipayTradeFastpayRefundQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                retObj.putAll((Map) JSON.toJSON(response));
                retObj.put("isSuccess", true);
            } else {
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
}
