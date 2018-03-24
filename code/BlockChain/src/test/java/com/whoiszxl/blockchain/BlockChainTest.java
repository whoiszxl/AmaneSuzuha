package com.whoiszxl.blockchain;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.model.Block;
import com.whoiszxl.blockchain.model.Transaction;
import com.whoiszxl.blockchain.model.TransactionInput;
import com.whoiszxl.blockchain.model.TransactionOutput;
import com.whoiszxl.blockchain.model.Wallet;
import com.whoiszxl.blockchain.security.CryptoUtil;

public class BlockChainTest {
	
	/**
	 * 测试区块挖矿
	 * @throws Exception
	 */
	@Test
	public void testBlockMine() throws Exception {
		
		//创建一个空的区块链
		List<Block> blockchain = new ArrayList<Block>();
		
		//生成一个创世区块
		Block firstBlock = new Block(1, "1", System.currentTimeMillis(), new ArrayList<Transaction>(), 1, "1");
		
		//将创世区块加入到区块链中
		blockchain.add(firstBlock);
		
		System.out.println("创建完创世区块的区块链:"+JSON.toJSONString(blockchain));
		
		//创建一个空的交易集合
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction t1 = new Transaction();
		Transaction t2 = new Transaction();
		transactions.add(t1);
		transactions.add(t2);
		
		
		Wallet walletSender = Wallet.generateWallet();
		Wallet walletReciptent = Wallet.generateWallet();
		
		TransactionInput txIn = new TransactionInput(t2.getId(), 10, null, walletSender.getPublicKey());
		TransactionOutput txOut = new TransactionOutput(10, walletReciptent.getHashPubKey());
		
		Transaction tx3 = new Transaction(CryptoUtil.UUID(), txIn, txOut);
		
		//假设t2之前已经被记录到区块
		tx3.sign(walletSender.getPrivateKey(), t2);
		transactions.add(tx3);
		
		//创建一个系统奖励的交易
		Transaction sysTran = new Transaction();
		transactions.add(sysTran);
		
		//获取到链的最后一个区块
		Block latestBlock = blockchain.get(blockchain.size()-1);
		
		int nonce = 1;
		String hash;
		while(true) {
			//hash值计算  = SHA256(最后一个区块的hash + 交易记录信息 + 随机数)
			hash = CryptoUtil.SHA256(latestBlock.getHash() + JSON.toJSONString(transactions) + nonce);
			System.out.println(hash);
			if(hash.startsWith("0000")) {
				System.out.println("计算正确,计算次数为"+nonce+",hash:"+hash);
				break;
			}
			nonce++;
			System.out.println("计算错误,计算次数为"+nonce+",hash:"+hash);
		}
		
		Block block2 = new Block(latestBlock.getIndex()+1, hash, System.currentTimeMillis(), transactions, nonce, latestBlock.getHash());
		
		blockchain.add(block2);
		
		System.out.println("挖矿后的区块链:"+JSON.toJSONString(blockchain));
	}
}
