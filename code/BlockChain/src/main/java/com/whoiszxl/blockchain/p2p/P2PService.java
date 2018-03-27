package com.whoiszxl.blockchain.p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.model.Block;
import com.whoiszxl.blockchain.model.Transaction;
import com.whoiszxl.blockchain.model.Wallet;
import com.whoiszxl.blockchain.service.BlockService;

/**
 * p2p公共服务类
 * @author whoiszxl
 *
 */
@Service
public class P2PService {

	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	
	@Autowired
	private BlockService blockService;
	
	//查询最新的区块
	public final static int QUERY_LATEST_BLOCK = 0;
	//查询整个区块链
	public final static int QUERY_BLOCKCHAIN = 1;
	//查询交易集合
	public final static int QUERY_TRANSACTION = 2;
	//查询已打包交易集合
	public final static int QUERY_PACKED_TRANSACTION = 3;
	//查询钱包集合
	public final static int QUERY_WALLET = 4;
	//返回区块集合
	public final static int RESPONSE_BLOCKCHAIN = 5;
	//返回交易集合
	public final static int RESPONSE_TRANSACTION = 6;
	//返回已打包交易集合
	public final static int RESPONSE_PACKED_TRANSACTION = 7;
	//返回钱包集合
	public final static int RESPONSE_WALLET = 8;
	
	public List<WebSocket> getSockets() {
		return sockets;
	}
	
	/**
	 * 
	 * @param webSocket
	 * @param msg
	 * @param sockets
	 */
	public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
		try {
			//获取到传入的信息对象
			Message message = JSON.parseObject(msg, Message.class);
			System.out.println("接收到" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息"
			        + JSON.toJSONString(message));
			switch (message.getType()) {
			case QUERY_LATEST_BLOCK:
				write(webSocket, responseLatestBlockMsg());
				break;
			case QUERY_BLOCKCHAIN:
				write(webSocket, responseBlockChainMsg());
				break;
			case QUERY_TRANSACTION:
				write(webSocket, responseTransactions());
				break;
			case QUERY_PACKED_TRANSACTION:
				write(webSocket, responsePackedTransactions());
				break;
			case QUERY_WALLET:
				write(webSocket, responseWallets());
				break;
			case RESPONSE_BLOCKCHAIN:
				handleBlockChainResponse(message.getData(), sockets);
				break;
			case RESPONSE_TRANSACTION:
				handleTransactionResponse(message.getData());
				break;
			case RESPONSE_PACKED_TRANSACTION:
				handlePackedTransactionResponse(message.getData());
				break;
			case RESPONSE_WALLET:
				handleWalletResponse(message.getData());
				break;
			}
		} catch (Exception e) {
			System.out.println("处理p2p消息错误:" + e.getMessage());
		}
	}
	
	/**
	 * 处理区块链响应
	 * @param message
	 * @param sockets
	 */
	public synchronized void handleBlockChainResponse(String message, List<WebSocket> sockets) {
		//获取到消息,解析成blocklist
		List<Block> receiveBlockchain = JSON.parseArray(message, Block.class);
		//对index进行排序
		Collections.sort(receiveBlockchain, new Comparator<Block>() {
			public int compare(Block block1, Block block2) {
				return block1.getIndex() - block2.getIndex();
			}
		});

		//获取到json中最新的一个区块
		Block latestBlockReceived = receiveBlockchain.get(receiveBlockchain.size() - 1);
		//获取到实际区块链中的最新的一个区块
		Block latestBlock = blockService.getLatestBlock();
		//如果json接收的最后一个区块的index大于实际区块链的索引
		if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
			//判断实际区块链最后一个区块的hash是否等于接收到的json区块的新区快的前一个区块
			if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
				System.out.println("将新接收到的区块加入到本地的区块链");
				//将新接收的区块添加到到本地区块链中并将最新区块广播除去
				if (blockService.addBlock(latestBlockReceived)) {
					broatcast(responseLatestBlockMsg());
				}
			} else if (receiveBlockchain.size() == 1) {
				//接收的区块只有一个
				System.out.println("查询所有通讯节点上的区块链");
				broatcast(queryBlockChainMsg());
			} else {
				// 用长链替换本地的短链
				blockService.replaceChain(receiveBlockchain);
			}
		} else {
			System.out.println("接收到的区块链不比本地区块链长，不处理");
		}
	}

	/**
	 * 处理钱包响应
	 * @param message
	 */
	public void handleWalletResponse(String message) {
		//获取到钱包消息
		List<Wallet> wallets = JSON.parseArray(message, Wallet.class);
		wallets.forEach(wallet -> {
			//将钱包地址和钱包对象添加到其它钱包中
			blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
		});
	}

	/**
	 * 处理交易响应,将p2p广播收到的交易添加到所有交易中
	 * @param message
	 */
	public void handleTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message, Transaction.class);
		blockService.getAllTransactions().addAll(txs);
	}
	
	/**
	 * 处理已打包交易响应
	 * @param message
	 */
	public void handlePackedTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message, Transaction.class);
		blockService.getPackedTransactions().addAll(txs);
	}

	/**
	 * 给ws发送消息
	 * @param ws
	 * @param message
	 */
	public void write(WebSocket ws, String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
		ws.send(message);
	}

	public void broatcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}

	/**
	 * 查询整个区块链
	 * @return
	 */
	public String queryBlockChainMsg() {
		return JSON.toJSONString(new Message(QUERY_BLOCKCHAIN));
	}

	public String queryLatestBlockMsg() {
		return JSON.toJSONString(new Message(QUERY_LATEST_BLOCK));
	}
	
	public String queryTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_TRANSACTION));
	}
	
	public String queryPackedTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_PACKED_TRANSACTION));
	}
	
	public String queryWalletMsg() {
		return JSON.toJSONString(new Message(QUERY_WALLET));
	}

	public String responseBlockChainMsg() {
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain())));
	}

	/**
	 * 最新的区块
	 * @return
	 */
	public String responseLatestBlockMsg() {
		Block[] blocks = { blockService.getLatestBlock() };
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)));
	}
	
	/**
	 * 所有的交易
	 * @return
	 */
	public String responseTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_TRANSACTION, JSON.toJSONString(blockService.getAllTransactions())));
	}
	
	/**
	 * 已打包交易
	 * @return
	 */
	public String responsePackedTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION, JSON.toJSONString(blockService.getPackedTransactions())));
	}
	
	/**
	 * 返回只有公钥的钱包json
	 * @return
	 */
	public String responseWallets() {
		List<Wallet> wallets = new ArrayList<Wallet>();
		//获取到只有公钥的钱包
		blockService.getMyWalletMap().forEach((address,wallet) -> {
			wallets.add(new Wallet(wallet.getPublicKey()));
		});
		//获取其它成员变量维护的只有公钥的钱包
		blockService.getOtherWalletMap().forEach((address,wallet) -> {
			wallets.add(wallet);
		});
		//返回所有钱包
		return JSON.toJSONString(new Message(RESPONSE_WALLET, JSON.toJSONString(wallets)));
	}
}
