package com.whoiszxl.blockchain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.model.Block;
import com.whoiszxl.blockchain.model.Transaction;
import com.whoiszxl.blockchain.model.TransactionInput;
import com.whoiszxl.blockchain.model.TransactionOutput;
import com.whoiszxl.blockchain.model.Wallet;
import com.whoiszxl.blockchain.security.CryptoUtil;

/**
 * 区块链核心服务
 * 
 * @author whoiszxl
 *
 */
@Service
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
		if(isValidNewBlock(newBlock, getLatestBlock())) {
			blockChain.add(newBlock);
			//新区块的交易需要加入到已打包的交易集合里面去
			packedTransactions.addAll(newBlock.getTransactions());
			return true;
		}
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
			//验证新区块hash值的正确性
			String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getTransactions(), newBlock.getNonce());
			if(!hash.equals(newBlock.getHash())){
				System.out.println("新区块的hash无效: " + hash + " " + newBlock.getHash());
				return false;
			}
			if(!isValidHash(newBlock.getHash())) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 验证hash值是否满足系统条件
	 * 
	 * @param hash
	 * @return
	 */
	private boolean isValidHash(String hash) {
		return hash.startsWith("0000");
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
	
	
	/**
	 * 验证整个区块链是否有效,通过循环从0,1开始遍历判断下去
	 * @param chain 整个区块链
	 * @return 是否有效
	 */
	private boolean isValidChain(List<Block> chain) {
		Block block = null;
		Block lastBlock = chain.get(0);
		int currentIndex = 1;
		
		while(currentIndex < chain.size()) {
			block = chain.get(currentIndex);
			
			if(!isValidNewBlock(block, lastBlock)) {
				return false;
			}
			
			lastBlock = block;
			currentIndex++;
		}
		return true;
	}
	
	/**
	 * 替换本地区块链
	 * @param newBlocks
	 */
	public void replaceChain(List<Block> newBlocks) {
		//需要新区块链有效并且新区块链区块数量大于当前区块链
		if(isValidChain(newBlocks) && newBlocks.size() > blockChain.size()){
			blockChain = newBlocks;
			
			//更新已打包交易集合
			packedTransactions.clear();
			//遍历出新区块链的所有交易,添加到packedTransactions中
			blockChain.forEach(block -> {
				packedTransactions.addAll(block.getTransactions());
			});
		}else {
			System.out.println("接收的新区块链无效哦");
		}
	}
	
	/**
	 * 创建一个新的区块并添加到链中
	 * @return
	 */
	private Block createNewBlock(int nonce, String previousHash, String hash, List<Transaction> blockTxs) {
		Block block = new Block(blockChain.size()+1, hash, System.currentTimeMillis(), blockTxs, nonce, previousHash);
		if(addBlock(block)) {
			return block;
		}
		return null;
	}
	
	/**
	 * 挖矿
	 * @param 要挖到哪个钱包地址
	 * @return
	 */
	public Block mine(String toAddress) {
		//创建系统奖励的交易
		allTransactions.add(newCoinbaseTx(toAddress));
		//取出已打包进区块的交易
		List<Transaction> blockTxs = new ArrayList<Transaction>(allTransactions);
		blockTxs.removeAll(packedTransactions);
		verifyAllTransactions(blockTxs);
		
		String newBlockHash = "";
		int nonce = 0;
		long start = System.currentTimeMillis();
		System.out.println("开始挖矿");
		while(true) {
			//计算新区块的hash值
			newBlockHash = calculateHash(getLatestBlock().getHash(), blockTxs, nonce);
			//校验hash值
			if(isValidHash(newBlockHash)){
				System.out.println("挖矿完成，正确的hash值：" + newBlockHash);
				System.out.println("挖矿耗费时间：" + (System.currentTimeMillis() - start) + "ms");
				break;
			}
			System.out.println("错误的hash值:" + newBlockHash);
			nonce++;
		}
		
		//创建新区块
		Block block = createNewBlock(nonce, getLatestBlock().getHash(), newBlockHash, blockTxs);
		return block;
	}
	
	
	
	/**
	 * 创建交易
	 * @param fromAddress 发送方
	 * @param toAddress 接收方
	 * @param amount 金额
	 * @return 创建后的交易
	 */
	public Transaction createTransaction(Wallet senderWallet, Wallet recipientWallet, int amount) {
		//查询到发送方的所有未话费交易
		List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
		//暂未实现找零, 遍历所有未花费交易然后判断转账金额是否和其中一笔对得上
		Transaction prevTx = null;
		for (Transaction transaction : unspentTxs) {
			//TODO 找零机制
			if(transaction.getTxOut().getValue() == amount) {
				prevTx = transaction;
				break;
			}
		}
		
		if(prevTx == null) {
			return null;
		}
		
		//创建tx交易,然后签名,存入所有交易集合中
		TransactionInput txIn = new TransactionInput(prevTx.getId(), amount, null, senderWallet.getPublicKey());
		TransactionOutput txOut = new TransactionOutput(amount, recipientWallet.getHashPubKey());
		Transaction transaction = new Transaction(CryptoUtil.UUID(), txIn, txOut);
		transaction.sign(senderWallet.getPrivateKey(), prevTx);
		allTransactions.add(transaction);
		return transaction;
	}
	
	
	
	/**
	 * 验证所有交易是否有效，非常重要的一步，可以防止双花
	 * @param blockTxs
	 */
	private void verifyAllTransactions(List<Transaction> blockTxs) {
		List<Transaction> invalidTxs = new ArrayList<>();
		for (Transaction tx : blockTxs) {
			if(!verifyTransaction(tx)){
				invalidTxs.add(tx);
			}
		}
		blockTxs.removeAll(invalidTxs);
		//去除无效交易
		allTransactions.removeAll(invalidTxs);
	}
	
	/**
	 * 通过交易的id找到这笔交易
	 * @param id 交易的id
	 * @return 这笔交易
	 */
	private Transaction findTransaction(String id) {
		for (Transaction tx : allTransactions) {
			if(id.equals(tx.getId())){
				return tx;
			}
		}
		return null;
	} 
	
	
	/**
	 * 验证这笔交易是否有效
	 * @param tx
	 * @return
	 */
	private boolean verifyTransaction(Transaction tx) {
		if(tx.coinbaseTx()) {
			return true;
		}
		//txInpt中的id为上一笔交易的id,获取到上一笔交易的id
		Transaction prevTx = findTransaction(tx.getTxIn().getTxId());
		//使用当前交易的verify验证交易是否有效
		return tx.verify(prevTx);
	}
	
	/**
	 * 生成一个区块奖励的交易
	 * @param toAddress 奖励存到哪个钱包地址
	 * @return 包含input,output的交易
	 */
	public Transaction newCoinbaseTx(String toAddress) {
		TransactionInput txIn = new TransactionInput("0", -1, null, null);
		Wallet wallet = myWalletMap.get(toAddress);
		//指定生成区块的奖励为10BTC
		TransactionOutput txOut = new TransactionOutput(10, wallet.getHashPubKey());
		return new Transaction(CryptoUtil.UUID(), txIn, txOut);
	}
	
	
	/**
	 * 创建一个钱包并存入全钱包节点集合
	 * @return 创建的钱包对象
	 */
	public Wallet createWallet() {
		Wallet wallet = Wallet.generateWallet();
		String address = wallet.getAddress();
		myWalletMap.put(address, wallet);
		return wallet;
	}
	
	
	/**
	 * 获取钱包余额
	 * @param address 钱包地址
	 * @return 钱包余额
	 */
	public int getWalletBalance(String address) {
		//获取到未花费交易集合
		List<Transaction> unspentTxs = findUnspentTransactions(address);
		int balance = 0;
		//遍历,将output的金额累加到balance
		for (Transaction transaction : unspentTxs) {
			balance += transaction.getTxOut().getValue();
		}
		return balance;
	}
	
	
	private List<Transaction> findUnspentTransactions(String address) {
		List<Transaction> unspentTxs = new ArrayList<Transaction>();
		Set<String> spentTxs = new HashSet<String>();
		for (Transaction tx : allTransactions) {
			if(tx.coinbaseTx()){
				continue;
			}
			//转出交易是已花费交易
			if(address.equals(Wallet.getAddress(tx.getTxIn().getPublicKey()))){
				spentTxs.add(tx.getTxIn().getTxId());
			}
		}
		
		/**
		 * allTransactions包含了未添加到区块中的交易
		 * blockChain未包含未添加到区块的交易
		 * 所以判断的时候需要将添加到区块的txId和未添加到区块的txInputId对比
		 * 要未添加到区块的上一笔txId不和添加到区块的txInputId一样才行
		 */
		for (Block block : blockChain) {
			List<Transaction> transactions = block.getTransactions();
			for (Transaction tx : transactions) {
				if(address.equals(CryptoUtil.MD5(tx.getTxOut().getPublicKeyHash()))){
					if(!spentTxs.contains(tx.getId())){
						unspentTxs.add(tx);
					}
				}
			}
		}
		
		return unspentTxs;
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
