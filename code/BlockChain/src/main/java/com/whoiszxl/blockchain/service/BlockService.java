package com.whoiszxl.blockchain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.model.Block;
import com.whoiszxl.blockchain.model.Transaction;
import com.whoiszxl.blockchain.model.Wallet;
import com.whoiszxl.blockchain.security.CryptoUtil;

/**
 * 区块链核心服务
 * 
 * @author whoiszxl
 *
 */
public class BlockService {

	/**
	 * 区块链存储结构
	 */
	private List<Block> blockChain = new ArrayList<Block>();

	/**
	 * 当前节点钱包集合
	 */
	private Map<String, Wallet> myWalletMap = new HashMap<String, Wallet>();

	/**
	 * 其他节点钱包集合，钱包只包含公钥
	 */
	private Map<String, Wallet> otherWalletMap = new HashMap<String, Wallet>();

	/**
	 * 转账交易集合
	 */
	private List<Transaction> allTransactions = new ArrayList<Transaction>();

	/**
	 * 已打包转账交易
	 */
	private List<Transaction> packedTransactions = new ArrayList<Transaction>();

	public BlockService() {
		// 创建创世区块
		Block genesisBlock = new Block(1, "1", System.currentTimeMillis(), new ArrayList<Transaction>(), 1, "1");
		blockChain.add(genesisBlock);
		System.out.println("生成了创世区块:" + JSON.toJSONString(genesisBlock));
	}
	
	/**
	 * 获取最新的区块,当前链上最后一个区块
	 * @return
	 */
	public Block getLatestBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}
	
	
	/**
	 * 添加新区块
	 * @param newBlock 需要添加的区块
	 * @return 是否添加成功
	 */
	public boolean addBlock(Block newBlock) {
		
		return false;
	}
	
	
	/**
	 * 验证新区块是否有效
	 * 
	 * @param newBlock 需要添加的block块
	 * @param previousBlock 最后的一个区块
	 * @return
	 */
	public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
		//将最后一个区块的哈希和新区块生成的哈希对比是否一致
		if(!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
			System.out.println("新区块的前一个区块hash验证不通过");
			return false;
		}else {
			//验证
		}
		
		
	}
	
	/**
	 * 计算区块的hash
	 * 
	 * @param previousHash 上一个区块的hash
	 * @param currentTransactions 当前区块的交易json
	 * @param nonce 计算量工作证明
	 * @return 新区块的hash值
	 */
	private String calculateHash(String previousHash, List<Transaction> currentTransactions, int nonce) {
		return CryptoUtil.SHA256(previousHash + JSON.toJSONString(currentTransactions) + nonce);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	public List<Block> getBlockChain() {
		return blockChain;
	}

	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}

	public Map<String, Wallet> getMyWalletMap() {
		return myWalletMap;
	}

	public void setMyWalletMap(Map<String, Wallet> myWalletMap) {
		this.myWalletMap = myWalletMap;
	}

	public Map<String, Wallet> getOtherWalletMap() {
		return otherWalletMap;
	}

	public void setOtherWalletMap(Map<String, Wallet> otherWalletMap) {
		this.otherWalletMap = otherWalletMap;
	}

	public List<Transaction> getAllTransactions() {
		return allTransactions;
	}

	public void setAllTransactions(List<Transaction> allTransactions) {
		this.allTransactions = allTransactions;
	}

	public List<Transaction> getPackedTransactions() {
		return packedTransactions;
	}

	public void setPackedTransactions(List<Transaction> packedTransactions) {
		this.packedTransactions = packedTransactions;
	}
	

}
