package com.whoiszxl.blockchain.security;

import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * hash加密工具类
 * @author whoiszxl
 *
 */
public class CryptoUtil {

	private CryptoUtil() {}
	
	/**
	 * sha256加密
	 * @param str 需要加密的字符串
	 * @return 加密后的字符串
	 */
	public static String SHA256(String str) {
		MessageDigest messageDigest;
		String encodeStr = "";
		
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr = byte2Hex(messageDigest.digest());
		} catch (Exception e) {
			System.out.println("getSHA256 is error" + e.getMessage());
		}
		
		return encodeStr;
	}
	
	/**
	 * md5加密并且返回一个从第四位截取的hash值
	 * @param str 需要加密的字符串
	 * @return 加密后的字符串
	 */
	public static String MD5(String str) {
		String result = DigestUtils.md5Hex(str);
		return result.substring(4, result.length());
	}
	
	/**
	 * 返回一个唯一随机数
	 * @return 唯一随机数
	 */
	public static String UUID() {
		return UUID.randomUUID().toString().replaceAll("\\-", "");
	}
	
	
	/**
	 * 字节转十六进制
	 * @param bytes 需要转换的字节数组
	 * @return 十六进制的字符串
	 */
	private static String byte2Hex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		String temp;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if(temp.length() == 1){
				builder.append("0");
			}
			builder.append(temp);
		}
		return builder.toString();
	}
}
