package org.xxpay.pay.channel.erzhanggui;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
public class RsaErzhangui {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"; //加密block需要预留11字节
    public static final int KEYBIT = 2048;
    public static final int RESERVEBYTES = 11;
    private KeyFactory keyFactory;
    private static Signature signature;
    private Cipher cipher;

    private int encryptBlock;
    private int decryptBlock;

    private static Map<String,Object> keys = null;

    public RsaErzhangui() {
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptBlock = KEYBIT / 8; //256 bytes
            encryptBlock = decryptBlock - RESERVEBYTES; //245 bytes
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 签名
     * @param plaintext 要签名的字符串
     * @param privateKey 私钥
     * @return 返回签名后的结果
     * @throws UnsupportedEncodingException 异常
     */
    public static String sign(String plaintext,String privateKey) throws UnsupportedEncodingException
    {
        String signBase64Str = "";
        try {
            // 载入秘钥
            signature.initSign(initPrivateKey(privateKey));
            signature.update(plaintext.getBytes("utf-8"));
            // base64编码
            signBase64Str = Base64.encodeBase64String(signature.sign());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signBase64Str;
    }

    /**
     * 验签
     * @param plaintext 验签的字符串
     * @param signBase64Str 验签的签名
     * @param publicKey 公钥
     * @return 验证成功或失败
     * @throws UnsupportedEncodingException 异常
     */
    public boolean verify(String plaintext, String signBase64Str,String publicKey) throws UnsupportedEncodingException
    {
        boolean isValid = false;
        try {
            //载入公钥
            signature.initVerify(initPublicKey(publicKey));
            signature.update(plaintext.getBytes("UTF-8"));
            //执行验签函数及base64解码
            isValid = signature.verify(Base64.decodeBase64(signBase64Str));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    /**
     * 加密
     * @param str_data 加密字符串
     * @param publicKey 公钥
     * @return 加密后的密文
     * @throws UnsupportedEncodingException 异常
     */
    public String encrypt(String str_data,String publicKey) throws UnsupportedEncodingException
    {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DeflaterOutputStream zos = new DeflaterOutputStream(bos);
            zos.write(str_data.getBytes("GBK"));
            zos.close();
            byte[] data= bos.toByteArray();
            //计算分段加密的block数 (向上取整)
            int nBlock = (data.length / encryptBlock);
            if ((data.length % encryptBlock) != 0) //余数非0block数再加1
            {
                nBlock += 1;
            }
            //输出buffer, 大小为nBlock个decryptBlock
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlock);
            cipher.init(Cipher.ENCRYPT_MODE, initPublicKey(publicKey));
            //分段加密
            for (int offset = 0; offset < data.length; offset += encryptBlock)
            {
                //block大小: encryptBlock 或 剩余字节数
                int inputLen = (data.length - offset);
                if (inputLen > encryptBlock)
                {
                    inputLen = encryptBlock;
                }
                //得到分段加密结果
                byte[] encryptedBlock = cipher.doFinal(data, offset, inputLen);
                //追加结果到输出buffer中
                outbuf.write(encryptedBlock);
            }
            return Base64.encodeBase64String(outbuf.toByteArray()); //ciphertext
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     * @param cryptedBase64Str 解密密文
     * @param privateKey 私钥
     * @return
     */
    public String decrypt(String cryptedBase64Str ,String privateKey)
    {
        //转换得到字节流
        byte[] data= Base64.decodeBase64(cryptedBase64Str);
        //计算分段解密的block数 (理论上应该能整除)
        int nBlock = (data.length / decryptBlock);
        //输出buffer, , 大小为nBlock个encryptBlock
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * encryptBlock);
        try {
            cipher.init(Cipher.DECRYPT_MODE, initPrivateKey(privateKey));
            //分段解密
            for (int offset = 0; offset < data.length; offset += decryptBlock)
            {
                //block大小: decryptBlock 或 剩余字节数
                int inputLen = (data.length - offset);
                if (inputLen > decryptBlock)
                {
                    inputLen = decryptBlock;
                }
                //得到分段解密结果
                byte[] decryptedBlock = cipher.doFinal(data, offset, inputLen);
                //追加结果到输出buffer中
                outbuf.write(decryptedBlock);
            }
            outbuf.flush();//---写完成后，需要刷新缓冲区，并且关闭缓冲
            outbuf.close();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InflaterOutputStream zos = new InflaterOutputStream(bos);
        try {
            zos.write(outbuf.toByteArray());
            zos.close();
            return new String(bos.toByteArray(), "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化自己的私钥
     * `openssl genrsa -out rsa_2048.key 2048` #指定生成的密钥的位数: 2048
     * `openssl pkcs8 -topk8 -inform PEM -in rsa_2048.key -outform PEM -nocrypt -out pkcs8.txt` #for Java 转换成PKCS#8编码
     * `openssl rsa -in rsa_2048.key -pubout -out rsa_2048_pub.key` #导出pubkey
     * @param privKeyPath  私钥路径
     * @param pubKeyPath   公钥路径
     * @param keysize 密钥长度, 默认2048
     */
//	public static void initKey(String privKeyPath, String pubKeyPath)
//	{
//		//读取私钥
//		if(KeysManage.get(privKeyPath)!=null){
//			//从内存读取私钥
//			localPrivKey =(PrivateKey)KeysManage.get(privKeyPath);
//			//System.out.println("--从内存读取私钥");
//		}else{
//			//从文件读取私钥
//			localPrivKey =initPrivateKey(privKeyPath);
//			KeysManage.put(privKeyPath, localPrivKey);
//			System.out.println("--从文件读取私钥");
//		}
//		// 读取公钥
//		if(KeysManage.get(pubKeyPath)!=null){
//			//从内存读取公钥
//			peerPubKey =(PublicKey) KeysManage.get(pubKeyPath);
//			//System.out.println("--从内存读取公钥");
//		}else{
//			//从文件读取公钥
//			peerPubKey = initPublicKey(pubKeyPath);
//			KeysManage.put(pubKeyPath, peerPubKey);
//			System.out.println("--从文件读取公钥");
//		}
//
//	}

    /**
     * 读取私钥
     */
    private static PrivateKey initPrivateKey(String privKeyPath) {
        try {
            BASE64Decoder base64decoder = new BASE64Decoder();
            byte[] keybyte = base64decoder.decodeBuffer(privKeyPath);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keybyte);
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取公钥
     */
    private static PublicKey initPublicKey(String pubKeyPath) {
        try {
            BASE64Decoder base64decoder = new BASE64Decoder();
            byte[] keybyte = base64decoder.decodeBuffer(pubKeyPath);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keybyte);
            return kf.generatePublic(keySpec);
        } catch (Exception e) {
        }
        return null;
    }
}
