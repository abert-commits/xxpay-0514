package org.xxpay.core.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * Created with IntelliJ IDEA.
 * User: zhanglei
 * Date: 18/3/1 下午6:05
 * Description: MD5加密工具类
 */
public class MD5Util {

    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = null;
        try {
            byteArray = inStr.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = md5Bytes[i] & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static void main(String[] args) {
        /**
         * loginid+ pwd
         */
        System.out.println(string2MD5("customerid=11809&sdorderno=ST00003&total_fee=297.00&remark=ceshi&paytype=alipay&notifyurl=http://sh.diyiym.com/php/notify_url.php\n" +
                "&returnurl=http://pay.diyiym.com/return_url.php&3273b0bc8486de87d01209b1385f06849cc39d60"));
    }

}
