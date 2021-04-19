package org.xxpay.pay.channel.funiukeji;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FuniukejiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(FuniukejiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_BINGZHANG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String code ;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝扫码
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doJuheRyPayReq(payOrder, "qrcode");//ALIPAYqr
                break;

            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
//        retObj = doJuheRyPayReq(payOrder, ""); //支付宝拼多多
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【新富科技统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
//            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("accessKey", channelPayConfig.getMchId());//商户号
            map.put("outTradeNo", payOrder.getPayOrderId());//订单号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()/100)); //请求时间戳
            map.put("quartetProductPayType", pay_code); //支付类型名称
            map.put("notifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("money", String.valueOf(payOrder.getAmount()));// 金额  act
            map.put("body", "手机支付");//商品名称
            String cs = XXPayUtil.mapToString(map);
            _log.info(logPrefix + " **********加密前参数{}",cs);
            String sign = createSign(channelPayConfig.getmD5Key(),map);
            _log.info(logPrefix+"sha2加密后{}"+sign);
            byte[] sign1 = sign.getBytes("utf-8");

//            sign = Base64Utils.encode(sign1);//Base64.encodeBase64String(b);
            _log.info(logPrefix +"***************BASE64编码后{}",sign);
            map.put("signature", sign);// md5签名 sha256_HMAC
            map.put("act", "qrcode");//商品名称
            _log.info(logPrefix + "******************sign:{}", sign);
            System.out.println(map);
            System.out.println(map.toString());
            System.out.println(channelPayConfig.getReqUrl());
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/PayServlet?act=preOrder",sendMsg);

//            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/sign_validate.jsp",sendMsg, "utf-8", "application/x-www-form-urlencoded");
            _log.info(logPrefix + "******************上游返回数据:{}", res);//   //Pay_Index.html
//            JSON.toJSONString()

            String payDayuUrl = null;
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("success").equals("ok")){
                payDayuUrl = resObj.getString("data");
                payDayuUrl=JSONObject.parseObject(payDayuUrl).getString("payUrl");
                System.out.println("支付跳转地址payUrl::::"+payDayuUrl);
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payDayuUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            }else {
                payInfo.put("errDes", "下单失败[" + res + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }


        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
        return payInfo;
    }

    public static String createSign(String accessSecretKey, Map<String, String> params) {
        String actualSign = null;
        if (params != null) {
            try {
                StringBuilder sb = new StringBuilder(1024);
                SortedMap<String, String> map = new TreeMap<>(params);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (StringUtils.isNoneEmpty(value)) {
                        sb.append(key).append('=').append(value).append('&');
                    }
                }
                // remove last '&':
                sb.deleteCharAt(sb.length() - 1);
//                System.out.println("preSign:"+sb.toString());

                // sign:
                Mac hmacSha256 = null;

                hmacSha256 = Mac.getInstance("HmacSHA256");

                SecretKeySpec secKey = new SecretKeySpec(accessSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                hmacSha256.init(secKey);
                String payload = sb.toString();
                byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                actualSign = Base64.getEncoder().encodeToString(hash);
//                System.out.println("BASE64后:"+actualSign);
//                actualSign = URLEncoder.encode(actualSign, "utf-8");
//                System.out.println("URLEncoder后:"+actualSign);

            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(FuniukejiPaymentService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(FuniukejiPaymentService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return  actualSign;

    }

    /**
     * 查询订单
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【新富科技订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("accessKey", channelPayConfig.getMchId());//商户号
            map.put("outTradeNo", payOrder.getPayOrderId());//订单号
            map.put("timestamp",  String.valueOf(System.currentTimeMillis()/100));//时间
            String sign = createSign( channelPayConfig.getmD5Key(),map);
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("signature", sign);// md5签名
            map.put("act", "queryOrderByOutTradeNo");// md5签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/PayServlet?act=preOrder",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("tradeState");
            if (code.equals("3")||code.equals("4")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }

            String status=resObj.getString("trade_state");
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "NOTPAY":
                return "未支付";
            case "SUCCESS":
                return "已支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}


