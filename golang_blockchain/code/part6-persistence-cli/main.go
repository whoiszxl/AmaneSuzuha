package main

import (
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part6-persistence-cli/BLC"
)

func main() {
    
    blockchain := BLC.CreateBlockchainWithGenesisBlock()
    cli := BLC.CLI{blockchain}
    cli.Run()
}