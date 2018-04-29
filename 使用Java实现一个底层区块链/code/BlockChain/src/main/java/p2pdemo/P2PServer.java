package p2pdemo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * p2p 服务器端
 * @author whoiszxl
 *
 */
public class P2PServer {
	
	/**
	 * 所有的websocket连接
	 */
	private List<WebSocket> sockets = new ArrayList<WebSocket>();
	
	public List<WebSocket> getSockets() {
		return sockets;
	}
	
	/**
	 * 初始化p2p网络
	 * @param port
	 */
	public void initP2PServer(int port) {
		//通过端口创建一个websocket服务
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				write(webSocket, "服务端连接成功");
				sockets.add(webSocket);
			}

			public void onClose(WebSocket webSocket, int i, String s, boolean b) {
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onMessage(WebSocket webSocket, String msg) {
				System.out.println("接收到客户端消息：" + msg);
				write(webSocket, "服务器收到消息");
				//broatcast("服务器收到消息:" + msg);
			}

			public void onError(WebSocket webSocket, Exception e) {
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				sockets.remove(webSocket);
			}

			public void onStart() {

			}
		};
		socketServer.start();
		System.out.println("listening websocket p2p port on: " + port);
	}
	
	/**
	 * 往 websocket写入消息
	 * @param ws
	 * @param message
	 */
	public void write(WebSocket ws, String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
		ws.send(message);
	}
	
	/**
	 * 广播所有节点消息
	 * @param message
	 */
	public void broatcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		//遍历所有websocket发送消息
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}

}

