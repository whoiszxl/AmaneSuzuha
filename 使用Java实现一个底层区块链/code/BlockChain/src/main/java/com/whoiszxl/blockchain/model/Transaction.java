package com.whoiszxl.blockchain.model;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.security.CryptoUtil;
import com.whoiszxl.blockchain.security.RSACoder;

/**
 * 交易记录模型
 * @author whoiszxl
 *
 */
public class Transaction {

	/**
	 * 交易唯一标识
	 */
	private String id;
	
	/**
	 * 交易输入
	 */
	private TransactionInput txIn;
	
	/**
	 * 交易输出
	 */
	private TransactionOutput txOut;
	
	
	/**
	 * 是否是系统生成区块的奖励交易
	 * @return
	 */
	public boolean coinbaseTx() {
		return txIn.getTxId().equals("0") && getTxIn().getValue() == -1;
	}
	
	/**
	 * 用私钥生成交易签名
	 * 
	 * @param privateKey 发送方私钥
	 * @param prevTx 未花费对象
	 */
	public void sign(String privateKey, Transaction prevTx) {
		//判断是否是奖励交易
		if (coinbaseTx()) {
			return;
		}

		// 判断上一笔未花费交易的id是否等于当前交易
		if (!prevTx.getId().equals(txIn.getTxId())) {
			System.err.println("交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
		}

		//克隆交易
		Transaction txClone = cloneTx();
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());
		String sign = "";
		try {
			sign = RSACoder.sign(txClone.hash().getBytes(), privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		txIn.setSignature(sign);
	}

	/**
	 * 生成用于交易签名的交易记录副本
	 * 
	 * @return
	 */
	public Transaction cloneTx() {
		//克隆input,传入当前交易输入的id,和value
		TransactionInput transactionInput = new TransactionInput(txIn.getTxId(), txIn.getValue(), null, null);
		TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(), txOut.getPublicKeyHash());
		return new Transaction(id, transactionInput, transactionOutput);
	}

	/**
	 * 验证交易签名
	 * 
	 * @param prevTx
	 * @return
	 */
	public boolean verify(Transaction prevTx) {
		if (coinbaseTx()) {
			return true;
		}
		System.out.println("是否能获取到上一笔交易:"+JSON.toJSONString(prevTx));
		//上一笔交易的id是否等于当前交易的input中的id
		if (!prevTx.getId().equals(txIn.getTxId())) {
			System.err.println("验证交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
		}

		Transaction txClone = cloneTx();
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());

		boolean result = false;
		try {
			result = RSACoder.verify(txClone.hash().getBytes(), txIn.getPublicKey(), txIn.getSignature());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 生成交易的hash
	 * 
	 * @return
	 */
	public String hash() {
		return CryptoUtil.SHA256(JSON.toJSONString(this));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Transaction() {
		super();
	}

	public Transaction(String id, TransactionInput txIn, TransactionOutput txOut) {
		super();
		this.id = id;
		this.txIn = txIn;
		this.txOut = txOut;
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TransactionInput getTxIn() {
		return txIn;
	}

	public void setTxIn(TransactionInput txIn) {
		this.txIn = txIn;
	}

	public TransactionOutput getTxOut() {
		return txOut;
	}

	public void setTxOut(TransactionOutput txOut) {
		this.txOut = txOut;
	}
	
	
}
