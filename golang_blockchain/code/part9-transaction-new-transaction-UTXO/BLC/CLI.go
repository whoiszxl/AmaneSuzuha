package BLC

import (
	"fmt"
	"os"
	"flag"
	"log"
)

type CLI struct {}

//使用说明
func printUsage()  {
	fmt.Println("Usage:")
	fmt.Println("1. createblockchain -address TO_ADDRESS 【创建带创世区块的区块链，并发放coinbase奖励到TO_ADDRESS】")
	fmt.Println("2. send -from FROM_ADDRESS -to TO_ADDRESS -amount SEND_AMOUNT 【发送一笔交易】")
	fmt.Println("3. printchain 【输出区块信息】")
	fmt.Println("4. getbalance -address -- 【获取余额】")
}

//判断是否有效
func isValidArgs()  {
	if len(os.Args) < 2 {
		printUsage()
		os.Exit(1)
	}
}


//cli的运行方法
func (cli *CLI) Run()  {

	//判断是否有效
	isValidArgs()

	//添加命令
	sendBlockCmd := flag.NewFlagSet("send",flag.ExitOnError)
	printChainCmd := flag.NewFlagSet("printchain",flag.ExitOnError)
	createBlockchainCmd := flag.NewFlagSet("createblockchain",flag.ExitOnError)
	getbalanceCmd := flag.NewFlagSet("getbalance",flag.ExitOnError)

	flagFrom := sendBlockCmd.String("from", "", "转账源地址")
	flagTo := sendBlockCmd.String("to","","转账目的地地址")
	flagAmount := sendBlockCmd.String("amount","","转账金额")
	
	//给命令添加data参数,并接收保存
	flagCreateBlockchainWithAddress := createBlockchainCmd.String("address","","创建创世区块的coinbase奖励地址")
	getbalanceWithAdress := getbalanceCmd.String("address","","要查询某一个地址的余额")

	switch os.Args[1] {
		case "send"://第一个参数为addblock
			err := sendBlockCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		case "printchain"://第一个参数为printchain
			err := printChainCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		case "createblockchain":
			err := createBlockchainCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		case "getbalance":
			err := getbalanceCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		default:
			printUsage()
			os.Exit(1)
	}

	if sendBlockCmd.Parsed() {
		if *flagFrom == "" || *flagTo == "" || *flagAmount == ""{
			printUsage()
			os.Exit(1)
		}
		
		//调用send进行新的区块挖矿
		from := JsonToArray(*flagFrom)
		to := JsonToArray(*flagTo)
		amount := JsonToArray(*flagAmount)
		cli.send(from,to,amount)
	}

	//printchain命令，输出所有区块
	if printChainCmd.Parsed() {
		cli.printchain()
	}

	//createblockchain命令，创建带创世区块的区块链
	if createBlockchainCmd.Parsed() {

		if *flagCreateBlockchainWithAddress == "" {
			fmt.Println("创世区块coinbase奖励地址不能为空")
			printUsage()
			os.Exit(1)
		}

		cli.createGenesisBlockchain(*flagCreateBlockchainWithAddress)
	}

	if getbalanceCmd.Parsed() {

		if *getbalanceWithAdress == "" {
			fmt.Println("地址不能为空....")
			printUsage()
			os.Exit(1)
		}

		cli.getBalance(*getbalanceWithAdress)
	}
}
