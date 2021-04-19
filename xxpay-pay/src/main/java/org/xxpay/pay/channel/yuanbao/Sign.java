package org.xxpay.pay.channel.yuanbao;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/** 
 * java签名算法 
 * 调用方法：Sign.get(data, key_id) data (amount + out_trade_no)，key_id 商户KEY 
 */
public class Sign {
    private static String factor = "ACDFHI123409KLNO";
    public static String encrypt(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
    public static String get(String data, String key_id) {
        String content = encrypt(data);
        char[] ar = content.toCharArray();
        char[] fa = factor.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < ar.length; i++) {
            if (isInteger(String.valueOf(ar[i]))) {
                stringBuffer.append(fa[Integer.parseInt(String.valueOf(ar[i]))]);
            }
        }
        return encrypt(content + key_id + stringBuffer.toString());
    }
}