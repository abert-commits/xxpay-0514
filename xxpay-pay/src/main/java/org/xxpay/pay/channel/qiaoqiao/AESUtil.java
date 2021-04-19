package org.xxpay.pay.channel.qiaoqiao;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESUtil{
	
	private static Logger logger = LoggerFactory.getLogger(AESUtil.class);

	//秘钥算法
	public static final String  keyAlgorithm = "AES";
	
	//初始化向量参数，AES 为16bytes
	public static final String iv = "alpayaesivvector";
	
	//加解密算法
	public static final String  cipherAlgorithm = "AES/CBC/PKCS5Padding";
	
	public static final String charSet = "UTF-8";
	
	/**
	 * AES加密
	 * @param plaintext 明文
	 * @param aesKey 私钥   AES固定格式为128/192/256 bits.即：16/24/32bytes
	 * @return 密文
	 * @throws Exception
	 */
	public static String AESEncrypt(String plaintext,String aesKey) throws Exception{
		
		//两个参数，第一个为私钥字节数组， 第二个为加密方式 AES
		Key keySpec = new SecretKeySpec(aesKey.getBytes(), keyAlgorithm);

		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
		
		//实例化加密类，参数为加密方式，要写全
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		/**
		 * //初始化，此方法可以采用三种方式，按服务器要求来添加。
		 * （1）无第三个参数
		 * （2）第三个参数为SecureRandom random = new SecureRandom();中random对象，随机数。(AES不可采用这种方法)
		 * （3）采用此代码中的IVParameterSpec
		 */
		cipher.init(Cipher.ENCRYPT_MODE,  keySpec, ivSpec);

		//cipher.init(Cipher.ENCRYPT_MODE, keySpec);

		//SecureRandom random = new SecureRandom();

		//cipher.init(Cipher.ENCRYPT_MODE, keySpec, random);

		//加密操作,返回加密后的字节数组，然后需要编码。主要编解码方式有Base64, HEX, UUE, 7bit等等。此处看服务器需要什么编码方式
		byte [] b = cipher.doFinal(plaintext.getBytes());
		//Base64、HEX等编解码
		String ciphertext = Base64.encodeBase64String(b);
		logger.info("AES加密的明文：{}，密文：{}", new Object[] { plaintext, ciphertext });
		return  ciphertext;
	}
	
	/**
	 *  AES解密
	 * @param ciphertext 密文
	 * @param aesKey 私钥   AES固定格式为128/192/256 bits.即：16/24/32bytes
	 * @return 明文
	 * @throws Exception
	 */
	public static String AESDecrypt(String ciphertext,String aesKey) throws Exception{

		 //先用Base64解码
		byte [] b = Base64.decodeBase64(ciphertext);

		IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());       

		Key keySpec = new SecretKeySpec(aesKey.getBytes(), keyAlgorithm);

		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);      //与加密时不同MODE:Cipher.DECRYPT_MODE

		byte [] plainByte = cipher.doFinal(b);
		String plaintext = new String(plainByte, charSet);
		logger.info("AES解密的密文：{}，明文：{}", new Object[] {ciphertext, plaintext });
		return plaintext;
	}
	
}
