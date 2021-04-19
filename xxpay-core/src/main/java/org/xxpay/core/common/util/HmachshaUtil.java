package org.xxpay.core.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HmachshaUtil {

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    public  static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }
    /**
     * sha256_HMAC加密
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    public static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
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
                    if (StringUtils.isNotEmpty(value)) {
                        sb.append(key).append('=').append(value).append('&');
                    }
                }
                // remove last '&':
                sb.deleteCharAt(sb.length() - 1);
                System.out.println("preSign:ssssss我知道了><><"+sb.toString());

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
                ex.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }

        return actualSign;

    }
}
