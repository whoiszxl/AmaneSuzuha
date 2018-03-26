package p2pdemo;

public class Main {
	public static void main(String[] args) {
		//创建websocket的服务端和客户端
		P2PServer p2pServer = new P2PServer();
		P2PClient p2pClient = new P2PClient();
		//获取运行参数中的端口
		int p2pPort = Integer.valueOf(args[0]);
		// 启动p2p服务端
		p2pServer.initP2PServer(p2pPort);
		//如果运行参数存在第二个,就要启动客户端并将客户端连接第二个参数的websocket服务
		if (args.length == 2 && args[1] != null) {
			// 作为p2p客户端连接p2p服务端
			p2pClient.connectToPeer(args[1]);
		}
	}
}

