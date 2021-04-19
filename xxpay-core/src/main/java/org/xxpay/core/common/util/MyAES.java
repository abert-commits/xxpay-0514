package org.xxpay.core.common.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * aes加解密
 */
public class MyAES {
	/*
     * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
     */
	private String key = "XxPayVip01266897";
	private String ivParameter = "0123456789abcdef";
	private static MyAES instance = new MyAES();

	private MyAES() {}

	public static MyAES getInstance() {
		return instance;
	}

	/**
	 * 加密
	 * @param sSrc
	 * @return
     */
	public String encrypt(String sSrc){
		String result = "";
		try {
			if (sSrc != null ){
				Cipher cipher;
				cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				byte[] raw = key.getBytes();
				SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
				IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
				byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
				result = new BASE64Encoder().encode(encrypted);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		// 此处使用BASE64做转码。
		return result;
				
	}

	/**
	 * 解密
	 * @param sSrc
	 * @return
     */
	public String decrypt(String sSrc){
		try {
			byte[] raw = key.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "utf-8");
			return originalString;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * aes加解密 用这个就好，不用blankj的
	 *
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String encryptAES(String data, String key, String iv) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			int blockSize = cipher.getBlockSize();
			data = replaceAllSpecial(data);
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;

			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);


			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			if (iv != null) {
				IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
				cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			}

			byte[] encrypted = cipher.doFinal(plaintext);

			return  new BASE64Encoder().encode(encrypted);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encryptAES(String data, String key, String method, String iv) {
		try {
			Cipher cipher = Cipher.getInstance(method);
			int blockSize = cipher.getBlockSize();
			data = replaceAllSpecial(data);
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;

			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);


			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			if (iv != null) {
				IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
				cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			}

			byte[] encrypted = cipher.doFinal(plaintext);

			return  new BASE64Encoder().encode(encrypted);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param data 密文
	 * @param key  密钥，长度16
	 * @param iv   偏移量，长度16
	 * @return 明文
	 * @author miracle.qu
	 */
	public static String decryptAES(String data, String key, String iv) {
		try {
			data = replaceAllSpecial(data);
			byte[] encrypted1 =new BASE64Decoder().decodeBuffer(data);// 先用base64解密
			// EncodeUtils.base64Decode(data);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			if (iv != null) {
				IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
				cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, keyspec);
			}


			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString.trim();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//把单\号换成空字符串
	public static String replaceAllSpecial(String s) {
		return s.replaceAll("\\\\", "");
	}


	public static void main(String[] args){
		// 需要加密的字串
		String cSrc = "095386";
		System.out.println(cSrc + "  长度为" + cSrc.length());
		// 加密
		long lStart = System.currentTimeMillis();
		String enString = MyAES.getInstance().encrypt(cSrc);
		System.out.println("加密后的字串是：" + enString + "长度为" + enString.length());

		long lUseTime = System.currentTimeMillis() - lStart;
		System.out.println("加密耗时：" + lUseTime + "毫秒");
		// 解密
		lStart = System.currentTimeMillis();
		String DeString = MyAES.getInstance().decrypt(enString);
		System.out.println("解密后的字串是：" + DeString);
		lUseTime = System.currentTimeMillis() - lStart;
		System.out.println("解密耗时：" + lUseTime + "毫秒");

	}

}