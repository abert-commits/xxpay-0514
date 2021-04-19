package org.xxpay.pay.channel.sandpay.sdk;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @Author: Chand
 * @Date: 13-12-04
 * @Time: 下午2:30
 * @Description: AES加密器
 */
public class AESTool {

    public static final String CHAR_ENCODING = "UTF-8";
    public static final String AES_ALGORITHM = "AES";

    /**
     * 加密
     *
     * @param data 待加密内容
     * @param key  加密秘钥
     * @return 十六进制字符串
     */
    public static String encrypt (String data, String key) {
        if (key.length() < 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        try {
            Cipher cipher      = Cipher.getInstance(AES_ALGORITHM);// 创建密码器
            byte[] byteContent = data.getBytes(CHAR_ENCODING);
            cipher.init(Cipher.ENCRYPT_MODE, genKey(key));// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result); // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 解密
     *
     * @param data 待解密内容(十六进制字符串)
     * @param key  加密秘钥
     * @return
     */
    public static String decrypt (String data, String key) {
        if (key.length() < 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        try {
            byte[] decryptFrom = parseHexStr2Byte(data);
            Cipher cipher      = Cipher.getInstance(AES_ALGORITHM);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, genKey(key));// 初始化
            byte[] result = cipher.doFinal(decryptFrom);
            return new String(result, "utf-8"); // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建加密解密密钥
     *
     * @param key 加密解密密钥
     * @return
     */
    private static SecretKeySpec genKey (String key) {
        SecretKeySpec secretKey;
        try {
            secretKey = new SecretKeySpec(key.getBytes(CHAR_ENCODING), AES_ALGORITHM);
            byte[]        enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey       = new SecretKeySpec(enCodeFormat, AES_ALGORITHM);
            return seckey;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("genKey fail!", e);
        }
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    private static String parseByte2HexStr (byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    private static byte[] parseHexStr2Byte (String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low  = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static void main (String[] args) throws Exception {
        String d   = UUID.randomUUID().toString();
        String key = "!1231231sdaf3ra中文gt23*(&(*^";
        String aes = AESTool.encrypt(key, d);
        System.out.println(aes);
        System.out.println(AESTool.decrypt(aes, d));
    }
}
