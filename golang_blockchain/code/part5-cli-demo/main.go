package main

import (
	"flag"
	"fmt"
	"os"
	"log"
)

func one() {
	flagString := flag.String("printchain", "空", "输出所有的区块信息.....")
	flagInt := flag.Int("number",6,"输出一个整数....")

	flagBool := flag.Bool("open",false,"判断真假....")

	flag.Parse()
	fmt.Printf("%s\n",*flagString)
	fmt.Printf("%d\n",*flagInt)
	fmt.Printf("%v\n",*flagBool)
}

func two() {
	args := os.Args;

	fmt.Printf("%v\n", args)
	fmt.Printf("%v\n", args[1])
}




func printUsage()  {
	
	fmt.Println("Usage:")
	fmt.Println("\taddBlock -data DATA -- 交易数据.")
	fmt.Println("\tprintchain -- 输出区块信息.")
	
}

func isValidArgs()  {
	if len(os.Args) < 2 {
		printUsage()
		os.Exit(1)
	}
}

func three() {
	//1.校验参数是否有效，如果没有参数就直接显示Usage
	isValidArgs()

	//2.定义俩命令
	addBlockCmd := flag.NewFlagSet("addBlock",flag.ExitOnError)
	printChainCmd := flag.NewFlagSet("printchain",flag.ExitOnError)

	//3.配置addBlock后的data命令
	flagAddBlockData := addBlockCmd.String("data","http://whoiszxl.com","交易数据......")

	//4.通过switch来获取第一个参数然后来对应解析第二个之后的参数
	switch os.Args[1] {
		case "addBlock":
			err := addBlockCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		case "printchain":
			err := printChainCmd.Parse(os.Args[2:])
			if err != nil {
				log.Panic(err)
			}
		default:
			printUsage()
			os.Exit(1)
	}

	//调用Parse后，如果data参数为空，报出使用手册
	if addBlockCmd.Parsed() {
		if *flagAddBlockData == "" {
			printUsage()
			os.Exit(1)
		}

		fmt.Println(*flagAddBlockData)
	}

	if printChainCmd.Parsed() {

		fmt.Println("输出所有区块的数据........")

	}
}

func main() {
	three()
}