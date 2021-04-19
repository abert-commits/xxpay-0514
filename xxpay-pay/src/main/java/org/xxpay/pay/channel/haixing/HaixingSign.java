package org.xxpay.pay.channel.haixing;

import org.xxpay.core.common.util.PayDigestUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HaixingSign {
    public static String parseStrToMd5L32(String str){
        String reStr = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());

            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : bytes){
                int bt = b&0xff;
                if (bt < 16){
                    stringBuffer.append(0);
                }
                stringBuffer.append(Integer.toHexString(bt));
            }
            reStr = stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return reStr;
    }

    public static String sign(String amount, String out_trade_no,String key_id){
        String data = parseStrToMd5L32(amount + out_trade_no);

        int[] box = new int[256];
        byte[] key = new byte[256];

        int pwd_length  = key_id.length();
        int data_length = data.length();

        for (int i=0;i<256;i++){
            key[i] = (byte)key_id.charAt(i % pwd_length);
            box[i] = i;
        }

        int i=0;
        int j=0;
        int tmp=0;
        for (i = 0; i < 256; i++){
            j = (j + box[i] + key[i]) % 256;
            tmp = box[i];
            box[i] = box[j];
            box[j] = tmp;
        }

        String cipher = "";
        int a=0;
        j=0;
        int k=0;

        for (i = 0; i < data_length; i++){
            a = (a + 1) % 256;
            j = (j + box[a]) % 256;

            tmp    = box[a];
            box[a] = box[j];
            box[j] = tmp;

            k = box[((box[a] + box[j]) % 256)];
            //cipher += (char)((byte)data.charAt(i) ^ k);
            cipher += ((byte)data.charAt(i) ^ k);
        }
        System.out.println("cipher:"+cipher);

        return parseStrToMd5L32(cipher);
    }

    public static void main(String[] args) {
        String amount       = "0.01";
        String out_trade_no = "2020061122595882524";
        String s_key        = "73189AA8D6BFAE";

        System.out.println("s_key:" + s_key);
        System.out.println("amount:" + amount);
        System.out.println("out_trade_no:" + out_trade_no);

        String sign = sign(amount, out_trade_no, s_key);

        System.out.println("sign-encode:"+sign);

        System.out.println("sign-realll:18da842e63cc26767e8f9c4faab8cc87");
    }



}
