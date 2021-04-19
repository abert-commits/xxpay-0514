package org.xxpay.core.common.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;
import java.util.Base64;


public class AESCBCUtil {
    // 加密
    public static String encrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        byte[] raw = sKey.getBytes();

        Key key = new SecretKeySpec(raw, "AES");
        Cipher in = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
        in.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivParameter.getBytes()));
        byte[] enc = in.doFinal(sSrc.getBytes());

        /*Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));*/
        return parseByte2HexStr(enc);// 此处使用BASE64做转码。
    }

    public static String decrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        try {


            byte[] raw = sKey.getBytes("utf-8");
            Key key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding","BC");
            cipher.init(Cipher.DECRYPT_MODE, key,  new IvParameterSpec(ivParameter.getBytes()));
            byte[] encrypted1 = parseHexStr2Byte(sSrc);//先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original,encodingFormat);
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将二进制转换成十六进制
     *
     * @param buf
     * @return
     */
    private static String parseByte2HexStr(byte buf[]) {
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
     * 将十六进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }
    }


    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String java_openssl_encrypt(String data, String password, String iv) throws Exception {
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            if (i < password.getBytes().length) {
                key[i] = password.getBytes()[i];
            } else {
                key[i] = 0;
            }
        }
        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        Security.addProvider(new BouncyCastleProvider());

        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv.getBytes()));
        Security.addProvider(new BouncyCastleProvider());

        String base64Str = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        Security.addProvider(new BouncyCastleProvider());

        return base64Str;

       /* Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec secretKey = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8), 0,
                cipher.getBlockSize());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        String sign = Base64.getEncoder().encodeToString(bytes);
        return sign;*/
    }

    public static String AES_cbc_decrypt(String encData, String password, String iv) throws Exception {

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            if (i < password.getBytes().length) {
                key[i] = password.getBytes()[i];
            } else {
                key[i] = 0;
            }
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);

        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv.getBytes()));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encData)));

    }

    public static void main(String[] args) throws Exception{
        //密钥 加密内容(对象序列化后的内容-json格式字符串)
        String content="{\"domain\":{\"method\":\"getDetails\",\"url\":\"http://www.baidu.com\"},\"name\":\"steadyjack_age\",\"age\":\"23\",\"address\":\"Canada\",\"id\":\"12\",\"phone\":\"15627284601\"}";

        String encryptText=java_openssl_encrypt(content,"zP50gBoWzDwVJEaNhaB8H9fKorcX4gIL","7eyARLcpbsocjl5P");
        String decryptText=java_openssl_encrypt(encryptText,"zP50gBoWzDwVJEaNhaB8H9fKorcX4gIL","7eyARLcpbsocjl5P");
        System.out.println(String.format("明文：%s \n加密结果：%s \n解密结果：%s ",content,encryptText,decryptText));
    }


}
