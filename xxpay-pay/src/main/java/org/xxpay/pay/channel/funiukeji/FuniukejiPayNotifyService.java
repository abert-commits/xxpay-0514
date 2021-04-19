package org.xxpay.pay.channel.funiukeji;//xingfukejiPayNotifyService    GetInput()

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayNotify;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.gepay.GepayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FuniukejiPayNotifyService extends BasePayNotify {

    private static final MyLog _log = MyLog.getLog(FuniukejiPayNotifyService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_FUNIUKEJI;
    }

    @Override
    public JSONObject doNotify(Object notifyData) {
        String logPrefix = "【处理新富科技回调】";
        _log.info("====== 开始处理新富科技回调通知 ======");
        HttpServletRequest request = (HttpServletRequest) notifyData;
        //获取到JSONObject
//        JSONObject jsonParam = PayDigestUtil.getJSONParam(request);
        // 初始化成功后的返回
        JSONObject retObj = buildRetObj();
        Map<String, Object> payContext = new HashMap();
        PayOrder payOrder;
        String respString = PayConstant.RETURN_ALIPAY_VALUE_SUCCESS;

        try {
            JSONObject params = new JSONObject();
            params.put("accessKey", request.getParameter("accessKey"));
            params.put("orderId", request.getParameter("orderId"));
            params.put("outTradeNo", request.getParameter("outTradeNo"));
            params.put("money", request.getParameter("money"));
            params.put("timestamp",request.getParameter("timestamp"));
            params.put("signature",request.getParameter("signature"));
            _log.info("支付回调=============" + params);
            _log.info("新富科技支付回调=============" + params);
            payContext.put("parameters", params);
            if (!verifyPayParams(payContext)) {
                retObj.put(PayConstant.RESPONSE_RESULT, GepayConfig.RETURN_VALUE_FAIL);
                return retObj;
            }
            payOrder = (PayOrder) payContext.get("payOrder");

            if (!CheckCallIP(request, payOrder.getPassageId(), payOrder)) {
                respString = "回调IP非白名单";
                retObj.put(PayConstant.RESPONSE_RESULT, respString);
                return retObj;
            }

            int updatePayOrderRows = 0;
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成,4-已退款

            if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId());
                if (updatePayOrderRows != 1) {
                    _log.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    retObj.put(PayConstant.RESPONSE_RESULT, "处理订单失败");
                    return retObj;
                }
                _log.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
            }

            // 业务系统后端通知
            if (updatePayOrderRows > 0) {
                baseNotify4MchPay.doNotify(payOrder, true);
                _log.info("====== 完成处理新富科技回调通知 ======");
            }
            respString = PayConstant.RETURN_VALUE_OK;
        } catch (Exception e) {
            _log.error(e, logPrefix + "处理异常");
        }
        retObj.put(PayConstant.RESPONSE_RESULT, respString);
        return retObj;
    }

    /**
     * 验证新富科技通知参数
     *
     * @return
     */
    public boolean verifyPayParams(Map<String, Object> payContext) throws Exception {
        JSONObject params = (JSONObject) payContext.get("parameters");
        // 校验结果是否成功
        String accessKey = params.getString("accessKey");  //商户号
        String money = params.getString("money"); // 交易金额
        String outTradeNo = params.getString("outTradeNo"); // 商户订单号
        String orderId = params.getString("orderId"); //交易流水号
        String timestamp = params.getString("timestamp"); //交易时间
        String signature = params.getString("signature");  //签名
//        signature = URLDecoder.decode(signature, "utf-8");

        // 查询payOrder记录
        String payOrderId = outTradeNo;
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            payContext.put("retMsg", "Can't found payOrder");
            return false;
        }

        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        // md5 加密
        String AccessKey = channelPayConfig.getMchId();
        if (AccessKey.equals(accessKey)){
            SortedMap map = XXPayUtil.JSONObjectToSortedMap(params);
            map.put("signature", signature);//
//            map.put("outTradeNo", "P01202009302157496961165");
            System.out.println("////////////////"+JSONObject.toJSONString(map));
//            System.out.println(verifySign(channelPayConfig.getmD5Key(),map));
            if (!verifySign(channelPayConfig.getmD5Key(),map)) {
                _log.error("验证签名失败. payOrderId={}, ", payOrderId);
                payContext.put("retMsg", "验证签名失败");
                return false;
            }
            // 核对金额

            long dbPayAmt = payOrder.getAmount().longValue();
            long resAmount = Long.valueOf(money);
            if (dbPayAmt != resAmount) {
                _log.error("金额不一致. outPayAmt={},payOrderId={}", money, payOrderId);
                payContext.put("retMsg", "金额不一致");
                return false;
            }
            payContext.put("payOrder", payOrder);
            return true;

        }else {
            payContext.put("payOrder", payOrder);
            return false;

        }

    }

    public static Boolean verifySign(String accessSecretKey, Map<String, String> params) {

        if (params != null) {
            try {
                StringBuilder sb = new StringBuilder(1024);
                String sign = params.get("signature");
                System.out.println("服务端签名："+sign);
                params.remove("signature");

                SortedMap<String, String> map = new TreeMap<>(params);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (StringUtils.isNotEmpty(value)) {
                        sb.append(key).append('=').append(value).append('&');
                    }
                }
                // remove last '&':
                sb.deleteCharAt(sb.length() - 1);
                // sign:
                Mac hmacSha256 = null;

                hmacSha256 = Mac.getInstance("HmacSHA256");

                SecretKeySpec secKey = new SecretKeySpec(accessSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                hmacSha256.init(secKey);
                String payload = sb.toString();
                byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                String actualSign = Base64.getEncoder().encodeToString(hash);
                _log.info("我的签名. -----------, "+actualSign);
                _log.info("回调签名. /--------------, "+sign);
                System.out.println("我的签名："+actualSign);
                System.out.println("签名："+sign);

                return actualSign.equals(sign);

            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(FuniukejiPayNotifyService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(FuniukejiPayNotifyService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return false;
    }

}
