package com.whoiszxl.blockchain.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.whoiszxl.blockchain.p2p.P2PClient;
import com.whoiszxl.blockchain.p2p.P2PServer;
import com.whoiszxl.blockchain.p2p.P2PService;
import com.whoiszxl.blockchain.service.BlockService;

/**
 * 初始化p2p服务
 * @author Administrator
 *
 */
@Component
public class InitP2PCommandLineRunner implements CommandLineRunner{

	@Autowired
	private BlockService blockService;
	
	@Autowired
	private P2PService p2pService;
	
	@Autowired
	private P2PServer p2pServer;
	
	@Autowired
	private P2PClient p2pClient;
	
	@Override
	public void run(String... args) throws Exception {
		if(args != null && (args.length == 1 || args.length == 2 || args.length == 3)) {
			try {
				// 启动p2p服务端
				int p2pPort = Integer.valueOf(args[1]);
				p2pServer.initP2PServer(p2pPort);
				if (args.length == 3 && args[2] != null) {
					// 作为p2p客户端连接p2p服务端
					p2pClient.connectToPeer(args[2]);
				}
				
			} catch (Exception e) {
				System.out.println("启动出错了:" + e.getMessage());
			}
		}else{
			System.out.println("usage: java -jar blockchain.jar 8081 7001");
		}
	}
}
