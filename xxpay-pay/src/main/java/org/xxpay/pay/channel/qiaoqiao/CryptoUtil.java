package org.xxpay.pay.channel.qiaoqiao;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CryptoUtil {
	
	private static Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
	
	//签名算法
	public static final String  signAlgorithm = "SHA1WithRSA";
	
	//秘钥算法
	public static final String  keyAlgorithm = "RSA";
	
	//加解密算法
	public static final String  cipherAlgorithm = "RSA/ECB/PKCS1Padding";
	
	public static final int  keySize = 2048;
	
	/**
	 * 数字签名函数入口
	 * 
	 * @param plainText
	 *            待签名明文
	 * @param privateKey
	 *            签名使用私钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 签名后的文本
	 * @throws Exception
	 */
	public static String digitalSign(String plainText, PrivateKey privateKey, String signAlgorithm) throws Exception {
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			byte [] plainBytes = plainText.getBytes();
			byte[] signBytes = digitalSign(plainBytes, privateKey, signAlgorithm);
			String signText = Base64.encodeBase64String(signBytes);
			logger.info("RSA生成签名的明文：{}，密文：{}", new Object[] { plainText, signText });
			return signText;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("数字签名时私钥无效");
		} catch (SignatureException e) {
			throw new Exception("数字签名时出现异常");
		}
	}
	
	/**
	 * 数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待签名明文字节数组
	 * @param privateKey
	 *            签名使用私钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 签名后的字节数组
	 * @throws Exception
	 */
	public static byte[] digitalSign(byte[] plainBytes, PrivateKey privateKey, String signAlgorithm) throws Exception {
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			signature.update(plainBytes);
			byte[] signBytes = signature.sign();
			return signBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("数字签名时私钥无效");
		} catch (SignatureException e) {
			throw new Exception("数字签名时出现异常");
		}
	}

	/**
	 * 验证数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待验签明文字节数组
	 * @param signBytes
	 *            待验签签名后字节数组
	 * @param publicKey
	 *            验签使用公钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 验签是否通过
	 * @throws Exception
	 */
	public static boolean verifyDigitalSign(String plainText, String signText, PublicKey publicKey, String signAlgorithm) throws Exception {
		logger.info("RSA验证签名的明文：{}，密文：{}", new Object[] { plainText, signText });
		boolean isValid = false;
		try {
			byte[] plainBytes = plainText.getBytes();
			byte[] signBytes = Base64.decodeBase64(signText);
			isValid = verifyDigitalSign(plainBytes, signBytes,  publicKey, signAlgorithm);
			return isValid;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("验证数字签名时公钥无效");
		} catch (SignatureException e) {
			throw new Exception("验证数字签名时出现异常");
		}
	}
	
	/**
	 * 验证数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待验签明文字节数组
	 * @param signBytes
	 *            待验签签名后字节数组
	 * @param publicKey
	 *            验签使用公钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 验签是否通过
	 * @throws Exception
	 */
	public static boolean verifyDigitalSign(byte[] plainBytes, byte[] signBytes, PublicKey publicKey, String signAlgorithm) throws Exception {
		boolean isValid = false;
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(publicKey);
			signature.update(plainBytes);
			isValid = signature.verify(signBytes);
			return isValid;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("验证数字签名时公钥无效");
		} catch (SignatureException e) {
			throw new Exception("验证数字签名时出现异常");
		}
	}

	/**
	 * 获取RSA公钥对象
	 * 
	 * @param filePath
	 *            RSA公钥路径
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA公钥对象
	 * @throws Exception
	 */
	public static PublicKey getRSAPublicKeyByFileSuffix(String filePath, String keyAlgorithm) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(sb.toString()));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PublicKey pubKey = keyFactory.generatePublic(pubX509);

			return pubKey;
		} catch (FileNotFoundException e) {
			throw new Exception("公钥路径文件不存在");
		} catch (IOException e) {
			throw new Exception("读取公钥异常");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", keyAlgorithm));
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成公钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 获取RSA公钥对象
	 * 
	 * @param  pubKyeStr
	 *            RSA公钥串
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA公钥对象
	 * @throws Exception
	 */
	public static PublicKey getRSAPublicKeyByPubKeyStr(String pubKyeStr, String keyAlgorithm) throws Exception {
		InputStream in = null;
		try {
			X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(pubKyeStr));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PublicKey pubKey = keyFactory.generatePublic(pubX509);

			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", keyAlgorithm));
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成公钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}


	/**
	 * 获取RSA私钥对象
	 * 
	 * @param filePath
	 *            RSA私钥路径
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA私钥对象
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static PrivateKey getRSAPrivateKeyByFileSuffix(String filePath, String keyAlgorithm) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(sb.toString()));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PrivateKey priKey = keyFactory.generatePrivate(priPKCS8);

			return priKey;
		} catch (FileNotFoundException e) {
			throw new Exception("私钥路径文件不存在");
		} catch (IOException e) {
			throw new Exception("读取私钥异常");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("生成私钥对象异常");
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成私钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 获取RSA私钥对象
	 * 
	 * @param priKeyStr
	 *            RSA私钥串
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA私钥对象
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static PrivateKey getRSAPrivateKeyByPriKeyStr(String priKeyStr, String keyAlgorithm) throws Exception {
		logger.info("RSA秘钥的key：{}", new Object[] { priKeyStr});
		InputStream in = null;
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(priKeyStr));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PrivateKey priKey = keyFactory.generatePrivate(priPKCS8);

			return priKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("生成私钥对象异常");
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成私钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * RSA加密
	 * 
	 * @param plainBytes
	 *            明文字节数组
	 * @param publicKey
	 *            公钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 加密后字节数组，不经base64编码
	 * @throws Exception
	 */
	public static byte[] RSAEncrypt(byte[] plainBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm) throws Exception {
		int keyByteSize = keyLength / 8; // 密钥字节数
		int encryptBlockSize = keyByteSize - reserveSize; // 加密块大小=密钥字节数-padding填充字节数
		int nBlock = plainBytes.length / encryptBlockSize;// 计算分段加密的block数，向上取整
		if ((plainBytes.length % encryptBlockSize) != 0) { // 余数非0，block数再加1
			nBlock += 1;
		}

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			// 输出buffer，大小为nBlock个keyByteSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
			// 分段加密
			for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
				int inputLen = plainBytes.length - offset;
				if (inputLen > encryptBlockSize) {
					inputLen = encryptBlockSize;
				}

				// 得到分段加密结果
				byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(encryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("没有[%s]此类加密算法", cipherAlgorithm));
		} catch (NoSuchPaddingException e) {
			throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("无效密钥");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("加密块大小不合法");
		} catch (BadPaddingException e) {
			throw new Exception("错误填充模式");
		} catch (IOException e) {
			throw new Exception("字节输出流异常");
		}
	}

	/**
	 * RSA解密
	 * 
	 * @param encryptedBytes
	 *            加密后字节数组
	 * @param privateKey
	 *            私钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 解密后字节数组，不经base64编码
	 * @throws Exception
	 */
	public static byte[] RSADecrypt(byte[] encryptedBytes, Key key, int keyLength, int reserveSize, String cipherAlgorithm) throws Exception {
		int keyByteSize = keyLength / 8; // 密钥字节数
		int decryptBlockSize = keyByteSize - reserveSize; // 解密块大小=密钥字节数-padding填充字节数
		int nBlock = encryptedBytes.length / keyByteSize;// 计算分段解密的block数，理论上能整除

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, key);

			// 输出buffer，大小为nBlock个decryptBlockSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlockSize);
			// 分段解密
			for (int offset = 0; offset < encryptedBytes.length; offset += keyByteSize) {
				// block大小: decryptBlock 或 剩余字节数
				int inputLen = encryptedBytes.length - offset;
				if (inputLen > keyByteSize) {
					inputLen = keyByteSize;
				}

				// 得到分段解密结果
				byte[] decryptedBlock = cipher.doFinal(encryptedBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(decryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("没有[%s]此类解密算法", cipherAlgorithm));
		} catch (NoSuchPaddingException e) {
			throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("无效密钥");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("解密块大小不合法");
		} catch (BadPaddingException e) {
			throw new Exception("错误填充模式");
		} catch (IOException e) {
			throw new Exception("字节输出流异常");
		}
	}

	public static void main(String[] args) {
		String plainText ="{dasdasdasdasdasdasdacsadnqsjjihiuhihi}";
		String privateKeyStr="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALcSdZmM3rt4DiMAemvOFJTUbifqYBAb/X7QBdxQCeU4UGD86KVw8NNODXdadoqzVdswy+NjAe+Iko4VQI1vePMVtktkGnhgx+EzZcqNQRFVj0T/lD8rks/m4gK8JBHHCqNws9zJ/G4WC8wIUAzJi2/cdotJyCy1AZBrPmlv55frAgMBAAECgYEAtoqWbiQTlvQPjIEWkFXtGbVznSNK4+U073R204WPSFrNctfbFdO2nctvC/pMxuIokqVmN3XqYTBZiYjRU/W5r1zjImsCU8EKUZyZRi6ljmKRBPa1vstGC3Bo3T6Z+9/1tcCaUxQwTK2Sbn4aL/7Df2Vh7AYrzIu2kA8Pa45vRtkCQQD30fVNGQ3bjIHx39TjXhtb9+mlDWk1gEiZIctdCi2jer133+VvyrF/K+3PovPceAy4yEoOLIC+YpoiIsZ9E313AkEAvR1j6BADiNT0/idWBhtOitsy6PBES4LEdHF6lkJijvOY3ZbHf1nKSx7UpBG0GrCSBmpUrd81G1v6D0V1NTRGLQJBANBfcPOXqmg9V5HJ09Yt7bFB3eoTQbBjoidoG/eqND+uV5tw3hlGhEJa7IXXDVcGdiP0/Re34bSzcchcFytZ9PcCQQCktsmCoRgDAMCV8LrrPLNvG7Y+zq4dOrtTVFdaMl3XdnIJZj9CO3mHbkX01PqSWIIHFmvEuOlvd+/Xhz6r5WjNAkB13ZIDZSsHVHzT39K0PRTmCHqwvcIaHYRXn0gJpewyEG3oxySK2Kk/Sh4jNaP0YWW9xnrxyNzj8JUqavr/VC/i";
		String publicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3EnWZjN67eA4jAHprzhSU1G4n6mAQG/1+0AXcUAnlOFBg/OilcPDTTg13WnaKs1XbMMvjYwHviJKOFUCNb3jzFbZLZBp4YMfhM2XKjUERVY9E/5Q/K5LP5uICvCQRxwqjcLPcyfxuFgvMCFAMyYtv3HaLScgstQGQaz5pb+eX6wIDAQAB";
		PrivateKey privateKey;
		try {
			privateKey = getRSAPrivateKeyByPriKeyStr(privateKeyStr, CryptoUtil.keyAlgorithm);
			String sign = digitalSign(plainText, privateKey,CryptoUtil.signAlgorithm);
			PublicKey publicKey = CryptoUtil.getRSAPublicKeyByPubKeyStr(publicKeyStr, CryptoUtil.keyAlgorithm);
			boolean flag = verifyDigitalSign(plainText, sign, publicKey,CryptoUtil.signAlgorithm);
			System.out.println(flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
