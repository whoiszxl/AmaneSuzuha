package com.whoiszxl.blockchain.model;

import java.util.Map;

import com.whoiszxl.blockchain.security.CryptoUtil;
import com.whoiszxl.blockchain.security.RSACoder;

/**
 * 钱包
 * @author whoiszxl
 *
 */
public class Wallet {

	/**
	 * 公钥
	 */
	private String publicKey;
	
	/**
	 * 私钥
	 */
	private String privateKey;

	/**
	 * 空钱包构造
	 */
	public Wallet() {
	}
	
	
	/**
	 * 仅仅包含公钥的钱包,用来给其他节点使用,其他节点在转账的时候需要使用到
	 * @param publicKey
	 */
	public Wallet(String publicKey) {
		super();
		this.publicKey = publicKey;
	}
	
	/**
	 * 含有公钥私钥的构造
	 * @param publicKey 公钥
	 * @param privateKey 私钥
	 */
	public Wallet(String publicKey, String privateKey) {
		super();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * 生成一个包含公钥私钥的钱包
	 * @return
	 */
	public static Wallet generateWallet() {
		Map<String, Object> initKey;
		try {
			//本地生成公钥私钥对
			initKey = RSACoder.initKey();
			//获取一个通过base64加密过的公钥和秘钥
			String publicKey = RSACoder.getPublicKey(initKey);
			String privateKey = RSACoder.getPrivateKey(initKey);
			return new Wallet(publicKey, privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取钱包的地址,通过md5加密
	 * @return 通过md5加密后的短钱包地址
	 */
	public String getAddress() {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}
	
	/**
	 * 根据钱包公钥生成钱包地址,通过md5加密
	 * 
	 * @param publicKey 钱包公钥
	 * @return 通过md5加密后的短钱包地址
	 */
	public static String getAddress(String publicKey) {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}
	
	/**
	 * 获取钱包公钥hash
	 * 
	 * @return hash加密后的钱包公钥
	 */
	public String getHashPubKey() {
		return CryptoUtil.SHA256(publicKey);
	}
	
	
	/**
	 * 生成钱包公钥的hash
	 * @param publicKey 钱包公钥
	 * @return hash 加密后的公钥
	 */
	public static String hashPubKey(String publicKey) {
		return CryptoUtil.SHA256(publicKey);
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	
}
