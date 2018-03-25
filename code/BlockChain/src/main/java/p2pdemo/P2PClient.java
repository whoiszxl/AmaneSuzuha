package p2pdemo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * p2p客户端
 * @author whoiszxl
 *
 */
public class P2PClient {
	
	/**
	 * socket连接池
	 */
	private List<WebSocket> sockets = new ArrayList<WebSocket>();

	public List<WebSocket> getSockets() {
		return sockets;
	}

	/**
	 * 连接到个人
	 * @param peer ws://localhost socket地址
	 */
	public void connectToPeer(String peer) { 
		try {
			//创建一个websocket连接,连接到peer地址
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					write(this, "客户端连接成功");
					sockets.add(this);
				}

				@Override
				public void onMessage(String msg) {
					System.out.println("收到服务端发送的消息:" + msg);
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					System.out.println("connection failed");
					sockets.remove(this);
				}

				@Override
				public void onError(Exception e) {
					System.out.println("connection failed");
					sockets.remove(this);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}
	
	/**
	 * 往websocket发送string消息
	 * @param ws
	 * @param message
	 */
	public void write(WebSocket ws, String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息:" + message);
		ws.send(message);
	}
	
	/**
	 * 广播消息
	 * @param message
	 */
	public void broatcast(String message) {
		if (sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		//获取到socket连接池里所有socket发送消息
		for (WebSocket socket : sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}

}
