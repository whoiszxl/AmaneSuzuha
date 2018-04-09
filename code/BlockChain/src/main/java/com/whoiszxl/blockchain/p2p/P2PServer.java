package com.whoiszxl.blockchain.p2p;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * p2p服务器端
 * @author whoiszxl
 *
 */
@Service
public class P2PServer {

	@Autowired
	private P2PService p2pService;
	
	/**
	 * 初始化p2p服务端
	 * @param port 服务端开启的端口号
	 */
	public void initP2PServer(int port) {
		//通过端口创建一个websocket的服务端
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				//在socket连接开启的时候将当前服务器端websocket添加到sockets集合中
				p2pService.getSockets().add(webSocket);
			}

			public void onClose(WebSocket webSocket, int i, String s, boolean b) {
				//关闭的时候直接移除
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				p2pService.getSockets().remove(webSocket);
			}

			public void onMessage(WebSocket webSocket, String msg) {
				//接收到消息的时候根据消息类型对区块链做出操作
				p2pService.handleMessage(webSocket, msg, p2pService.getSockets());
			}

			public void onError(WebSocket webSocket, Exception e) {
				//报错移除socket
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				p2pService.getSockets().remove(webSocket);
			}

			public void onStart() {

			}
		};
		socketServer.start();
		System.out.println("listening websocket p2p port on: " + port);
	}
}
