package com.whoiszxl.blockchain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 区块链对外提供服务接口控制器
 * @author whoiszxl
 *
 */
public class ChainController {

	
	
	/**
	 * 查询区块链
	 * @return
	 */
	@GetMapping("/chain")
	@ResponseBody
	public String chain() {
		
		return "";
	}
	
	
	/**
	 * 创建钱包
	 * @return
	 */
	@PostMapping("/wallet/create")
	@ResponseBody
	public String createWallet() {
		
		return "";
	}
	
	/**
	 * 查询钱包
	 * @return
	 */
	@GetMapping("/wallet/get")
	@ResponseBody
	public String getWallet() {
		
		return "";
	}
	
	/**
	 * 挖矿
	 * @return
	 */
	@PostMapping("/mine")
	@ResponseBody
	public String mine() {
		
		return "";
	}
	
	/**
	 * 转账交易
	 * @return
	 */
	@PostMapping("/transactions/new")
	@ResponseBody
	public String newtransaction() {
		
		return "";
	}
	
	/**
	 * 查询未打包交易
	 * @return
	 */
	@GetMapping("/transactions/unpacked/get")
	@ResponseBody
	public String getUnpackedTransaction() {
		
		return "";
	}
	
	/**
	 * 查询钱包余额
	 * @return
	 */
	@GetMapping("/wallet/balance/get")
	@ResponseBody
	public String getWalletBalance() {
		
		return "";
	}
	
	/**
	 * 查询所有socket节点
	 * @return
	 */
	@GetMapping("/peers")
	@ResponseBody
	public String peers() {
		
		return "";
	}
	
}
