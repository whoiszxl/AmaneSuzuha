package com.whoiszxl.blockchain.p2p;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * p2p客户端
 * @author whoiszxl
 *
 */
@Service
public class P2PClient {
	
	@Autowired
	private P2PService p2pService;
	
	public void connectToPeer(String peer) {
		try {
			//创建一个websocket的客户端,并传入需要连接的服务端的uri地址
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					//在open回调中写入区块链的一些信息并将当前的p2p客户端添加到sockets集合中
					p2pService.write(this, p2pService.queryLatestBlockMsg());
					p2pService.write(this, p2pService.queryTransactionMsg());
					p2pService.write(this, p2pService.queryPackedTransactionMsg());
					p2pService.write(this, p2pService.queryWalletMsg());
					p2pService.getSockets().add(this);
				}

				@Override
				public void onMessage(String msg) {
					//在接收到消息的时候将消息写入到客户端
					p2pService.handleMessage(this, msg, p2pService.getSockets());
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					//关闭的时候移除掉sockets集合中的当前客户端
					System.out.println("connection failed");
					p2pService.getSockets().remove(this);
				}

				@Override
				public void onError(Exception e) {
					//出错的时候移除掉sockets集合中的当前客户端
					System.out.println("connection failed");
					p2pService.getSockets().remove(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}
}
