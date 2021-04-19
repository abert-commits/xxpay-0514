package org.xxpay.pay.channel.taibai;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class LibaipayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LibaipayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LIBAI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //微信H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doMayiPayReq(payOrder, "3");
                break;
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doMayiPayReq(payOrder, "2");
                break;
            //银行卡
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doMayiPayReq(payOrder, "1");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doMayiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【李白付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("platformCode", channelPayConfig.getMchId());//商户编号
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//当前系统时间戳，以北京时间为准，到毫秒级

            String publicKey = channelPayConfig.getRsaPublicKey(); //代替 userAccount,userId
            String[] user = publicKey.split(",");

            map.put("thirdOrderId", payOrder.getPayOrderId());//商户订单id
            map.put("userAccount", user[0]);//商户用户账号
            map.put("userId", user[1]);//商户用户id

            map.put("cash", "1");//法币币种id：1 CNY(人民币);2 USD(美元)
            map.put("money", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));// 金额 单位：元
            map.put("payType", channel);//支付方式（1：银行卡，2：支付宝，3：微 信）
            map.put("mark", "货款");//备注
            map.put("networkIp", "127.0.0.1");// 用户所在网络ip
            String sendMsg = DES.encrypt(mapToString(map).replace(">", ""), "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String rsaprivateKey = channelPayConfig.getRsaprivateKey(); //代替 下单的 nounce:985350 encryptKey:bf7ff6e953be360d10e8eed9fbc2cff0
            String[] nounce_encryptKey = rsaprivateKey.split(",");
            String sign = getSign(map, nounce_encryptKey[0], nounce_encryptKey[1]);


            _log.info(logPrefix + "******************sign:{}", sign);
            Map map1 = new HashMap();
            map1.put("param", sendMsg); //加密后数据
            map1.put("sign", sign); //生成好的签名
            map1.put("tradeType", "1"); //加密后数据
            String urlParam = XXPayUtil.mapToString(map1).replace(">", "");
            String url = channelPayConfig.getReqUrl() + "/index.html?" + urlParam;

           /* _log.info("上游返回信息：" + res);
            if (!res.contains("</")) {
                JSONObject resObj = JSONObject.parseObject(res);
                String retMsg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            JSONObject payParams = new JSONObject();*/
/*
            String payJumpUrl = res;
*/
            /*if (payOrder.getChannelId().contains("SDK")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else if (res.contains("form")) {
                payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            } else {
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            }
*/
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl", url);
            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
        return payInfo;
    }

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【李白支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("platformCode", channelPayConfig.getMchId());//商户编号
            map.put("thirdOrderId", payOrder.getPayOrderId());//上游訂單號
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));//当前系统时间戳，以北京时间为准，到毫秒级

            String sendMsg = DES.encrypt(mapToString(map).replace(">", ""), "");
            String getmD5Key = channelPayConfig.getmD5Key(); //订单详情
            String[] get = getmD5Key.split(",");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String sign = getSign(map, get[0], get[1]);
            _log.info(logPrefix + "******************sign:{}", sign);
            Map map1 = new HashMap();
            map1.put("param", sendMsg); //加密后数据
            map1.put("sign", sign); //生成好的签名

            String urlParam = XXPayUtil.mapToString(map1).replace(">", "");

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/indexDetail.html?"+urlParam);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("trade_state");
            if (resObj.containsKey("status") && resObj.getString("status").equals("error")) {
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
            if (retCode.equals("SUCCESS")) {
                // 订单成功
                BigDecimal amount = resObj.getBigDecimal("amount");
                // 核对金额
                long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                long dbPayAmt = payOrder.getAmount().longValue();
                if (dbPayAmt == payOrder.getAmount()) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    //map 转string 以$拼接
    public static String mapToString(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        String result = "";
        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || value.equals("")) {
                continue;
            }
            sb.append(key + "=" + value + "&&");
        }
        result = sb.toString().substring(0, sb.length() - 2);
        _log.info("Sign Before URL:" + result);
        return result;
    }

    public static String getSign(Map<String, Object> map, String nounce, String encrypt_key) throws UnsupportedEncodingException {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue() && !"".equals(entry.getValue())) {
                if (entry.getValue() instanceof JSONObject) {
                    list.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(getSortJson((JSONObject) entry.getValue()), "UTF-8") + ".");
                } else {
                    list.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8") + ".");
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result = result.substring(0, result.length() - 1);
        _log.info("Sign Before URL:" + result);
        String sha1 = genHMAC(result, encrypt_key);
        _log.info("Sign Before sha1:" + sha1);
        result = PayDigestUtil.md5(nounce + "." + sha1, "UTF-8").toLowerCase();
        _log.info("Sign md5:" + result);
        return result;
    }

    public static String getSortJson(JSONObject obj) {
        SortedMap map = new TreeMap();
        Set<String> keySet = obj.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            Object vlaue = obj.get(key);
            map.put(key, vlaue);
        }
        return JSONObject.toJSONString(map);
    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * 使用 HMAC-SHA1 签名方法对data进行签名
     *
     * @param data 被签名的字符串
     * @param key  密钥
     * @return 加密后的字符串
     */
    public static String genHMAC(String data, String key) {
        String result = null;
        try {
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            //用给定密钥初始化 Mac 对象
            mac.init(signinKey);
            //完成 Mac 操作
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64Utils.encode(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        } catch (InvalidKeyException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
