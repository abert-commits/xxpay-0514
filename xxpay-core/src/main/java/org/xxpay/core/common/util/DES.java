package org.xxpay.core.common.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by 32127 on 2019/6/29.
 */
public class DES {
    /**
     * 加密
     *
     * @param s
     * @return
     */
    public static String encrypt(String s) {
        return encrypt(s, null);
    }

    /**
     * 解密
     *
     * @param s
     * @return
     */
    public static String decrypt(String s) {
        return decrypt(s, null);
    }

    public static byte[] encrypt(byte[] s, byte[] key) {
        return encrypt(s, 1, key);
    }

    public static byte[] encrypt(byte[] s) {
        return encrypt(s, null);
    }

    public static byte[] decrypt(byte[] s, byte[] key) {
        return encrypt(s, 2, key);
    }

    public static byte[] decrypt(byte[] s) {
        return decrypt(s, null);
    }

    public static String encrypt(String s, String key) {
        if (s == null) {
            return null;
        }
        byte[] src = string2Bytes(s);
        byte[] b = encrypt(src, key == null ? null : string2Bytes(key));

        StringBuffer sb = new StringBuffer(1024);
        if ((b == null) || (b.length < 1)) {
            return null;
        }

        Random r = new Random(s.length());
        for (int i = 0; i < b.length; i++) {
            char c = (char) (r.nextInt(10) + 71);
            sb.append(c);
            if (b[i] < 0) {
                c = (char) (r.nextInt(10) + 81);
                b[i] = ((byte) -b[i]);
                sb.append(c);
            }
            sb.append(Integer.toString(b[i], 16).toUpperCase());
        }
        sb.deleteCharAt(0);
        return sb.toString();
    }

    public static String decrypt(String s, String key) {
        if (s.length() < 1) {
            return null;
        }
        String[] sByte = s.split("[G-Pg-p]");
        byte[] b = new byte[sByte.length];
        for (int i = 0; i < b.length; i++) {
            char c = sByte[i].charAt(0);
            if (((c >= 'Q') && (c <= 'Z')) || ((c >= 'q') && (c <= 'z'))) {
                b[i] = ((byte) -Byte.parseByte(sByte[i].substring(1), 16));
            } else {
                b[i] = Byte.parseByte(sByte[i], 16);
            }
        }
        byte[] ch = decrypt(b, key == null ? null : string2Bytes(key));
        if ((ch == null) || (ch.length < 1)) {
            return null;
        }
        return bytes2String(ch);
    }
    private static byte[] encrypt(byte[] s, int mode, byte[] salt) {
        if ((salt == null) || (salt.length < 8)) {
            byte[] nsalt = { -57, 115, 33, -116, 126, -56, -18, -103 };
            if (salt != null) {
                for (int i = 0; i < salt.length; i++) {
                    nsalt[i] = salt[i];
                }
            }
            salt = nsalt;
        }
        byte[] ciphertext;
        try {
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance("DES");
            DESKeySpec desKeySpec = new DESKeySpec(salt);
            SecretKey desKey = keyFac.generateSecret(desKeySpec);

            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(mode, desKey);

            ciphertext = cipher.doFinal(s);
        } catch (Exception e) {
            ciphertext = null;
        }
        return ciphertext;
    }
    private static byte[] string2Bytes(String str) {
        byte[] sb = null;
        try {
            sb = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            sb = str.getBytes();
        }
        return sb;
    }

    private static String bytes2String(byte[] bytes) {
        String s = null;
        try {
            s = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            s = new String(bytes);
        }
        return s;
    }


}
