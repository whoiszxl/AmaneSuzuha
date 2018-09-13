package BLC

import (
	"fmt"
	"os"
	"flag"
	"log"
)

type CLI struct {
	BC *Blockchain
}

//使用说明
func printUsage()  {
	fmt.Println("Usage:")
	fmt.Println("1. createblockchainwithgenesis -data -- 交易数据.")
	fmt.Println("2. addblock -data DATA -- 交易数据.")
	fmt.Println("3. printchain -- 输出区块信息.")
}

//判断是否有效
func isValidArgs()  {
	if len(os.Args) < 2 {
		printUsage()
		os.Exit(1)
	}
}

//添加一个区块
func (cli *CLI) addBlock(data string)  {
	cli.BC.AddBlockToBlockchain(data)
}

//打印所有区块
func (cli *CLI) printchain()  {
	cli.BC.PrintChain()
}

//cli的运行方法
func (cli *CLI) Run()  {

	//判断是否有效
	isValidArgs()

	//添加两个命令
	addBlockCmd := flag.NewFlagSet("addblock",flag.ExitOnError)
	printChainCmd := flag.NewFlagSet("printchain",flag.ExitOnError)

	//给addBlockCmd命令添加data参数
	flagAddBlockData := addBlockCmd.String("data","http://whoiszxl.com","交易数据......")

	switch os.Args[1] {
		case "addblock"://第一个参数为addblock
			err := addBlockCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		case "printchain"://第一个参数为printchain
			err := printChainCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		default:
			printUsage()
			os.Exit(1)
	}

	//如果-data为空，打印输出使用说明
	if addBlockCmd.Parsed() {
		if *flagAddBlockData == "" {
			printUsage()
			os.Exit(1)
		}
		//不然则调用添加区块的方法
		cli.addBlock(*flagAddBlockData)
	}

	//printchain命令，输出所有区块
	if printChainCmd.Parsed() {
		cli.printchain()
	}
}
