package com.whoiszxl.blockchain.controller;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.bo.Result;
import com.whoiszxl.blockchain.model.Block;
import com.whoiszxl.blockchain.model.Transaction;
import com.whoiszxl.blockchain.model.Wallet;
import com.whoiszxl.blockchain.p2p.Message;
import com.whoiszxl.blockchain.p2p.P2PService;
import com.whoiszxl.blockchain.service.BlockService;

/**
 * 区块链对外提供服务接口控制器
 * @author whoiszxl
 *
 */
@RestController
public class ChainController {

	@Autowired
	private BlockService blockService;
	
	@Autowired
	private P2PService p2pService;
	
	/**
	 * 查询区块链
	 * @return
	 */
	@GetMapping("/chain")
	@ResponseBody
	public List<Block> chain() {
		//直接调用service获取所有的区块
		return blockService.getBlockChain();
	}
	
	
	/**
	 * 创建钱包
	 * @return
	 */
	@PostMapping("/wallet/create")
	@ResponseBody
	public Result createWallet() {
		//调用服务创建一个钱包
		Wallet wallet = blockService.createWallet();
		//将只包含公钥的钱包创建一个数组
		Wallet[] wallets = {new Wallet(wallet.getPublicKey())};
		//通过p2p广播将只包含公钥的钱包广播出去
		String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_WALLET, JSON.toJSONString(wallets)));
		p2pService.broatcast(msg);
		return Result.Success("创建钱包成功,钱包地址为:" + wallet.getAddress());
	}
	
	/**
	 * 查询钱包
	 * @return
	 */
	@GetMapping("/wallet/get")
	@ResponseBody
	public Result getWallet() {
		//直接调用服务获取到所有的钱包集合
		return Result.Success("查询到所有钱包", blockService.getMyWalletMap().values());
	}
	
	/**
	 * 挖矿
	 * @return
	 */
	@PostMapping("/mine")
	@ResponseBody
	public Result mine(String address) {
		//通过钱包地址获取到这个地址的钱包对象
		Wallet myWallet = blockService.getMyWalletMap().get(address);
		//有效性验证
		if(myWallet == null) {
			return Result.Mistake("指定的钱包不存在");
		}
		//调用接口开始挖矿
		Block newBlock = blockService.mine(address);
		//有效性验证
		if(newBlock == null) {
			return Result.Mistake("挖矿失败,可能有其他节点已经挖出此块");
		}
		//将新的区块通过p2p广播出去
		Block[] blocks = {newBlock};
		String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
		p2pService.broatcast(msg);
		return Result.Success("挖矿生成了新的区块", newBlock);
	}
	
	/**
	 * 转账交易
	 * @return
	 */
	@PostMapping("/transactions/new")
	@ResponseBody
	public Result newtransaction(String sender, String recipient, int amount) {
		//通过钱包地址查询到发送方和接收方的钱包
		Wallet senderWallet = blockService.getMyWalletMap().get(sender);
		Wallet recipientWallet = blockService.getMyWalletMap().get(recipient);
		//如果接收方钱包不存在,就去查询只有公钥的其它钱包合集
		if(recipientWallet == null) {
			recipientWallet = blockService.getOtherWalletMap().get(recipient);
		}
		
		//有效性验证
		if(senderWallet == null) {
			return Result.Mistake("发送方钱包不存在");
		}
		if(recipientWallet == null) {
			return Result.Mistake("接收方钱包不存在");
		}
		
		//将发送方钱包和接收方钱包和金额创建一笔新交易
		Transaction newTransaction = blockService.createTransaction(senderWallet, recipientWallet, amount);
		if(newTransaction == null) {
			return Result.Mistake("钱包" + sender + "余额不足或该钱包找不到一笔等于" + amount + "BTC的UTXO");
		}else {
			//将新生成的交易广播出去
			Transaction[] txs = {newTransaction};
			String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_TRANSACTION, JSON.toJSONString(txs)));
			p2pService.broatcast(msg);
			return Result.Success("新生成了一笔交易!", newTransaction);
		}
	}
	
	/**
	 * 查询未打包交易
	 * @return
	 */
	@GetMapping("/transactions/unpacked/get")
	@ResponseBody
	public Result getUnpackedTransaction() {
		//获取到所有的交易
		List<Transaction> transactions = new ArrayList<Transaction>(blockService.getAllTransactions());
		//移除已经打包了的交易
		transactions.removeAll(blockService.getPackedTransactions());
		return Result.Success("查询到本节点未打包的交易:", transactions);
	}
	
	/**
	 * 查询钱包余额
	 * @return
	 */
	@GetMapping("/wallet/balance/get")
	@ResponseBody
	public Result getWalletBalance(String address) {
		//直接调用服务查询address钱包地址的余额
		return Result.Success("查询出了钱包余额", blockService.getWalletBalance(address));
	}
	
	/**
	 * 查询所有socket节点
	 * @return
	 */
	@GetMapping("/peers")
	@ResponseBody
	public Result peers() {
		//通过p2p服务查询到所有socket连接地址和端口
		List<String> socketAddressList = new ArrayList<String>();
		for (WebSocket socket : p2pService.getSockets()) {
			InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
			socketAddressList.add(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort() + "");
		}
		return Result.Success("查询到了所有的socket节点", socketAddressList);
	}
	
}
