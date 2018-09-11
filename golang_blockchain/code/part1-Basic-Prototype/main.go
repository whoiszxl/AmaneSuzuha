package main

import (
    "fmt"
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part1-Basic-Prototype/BLC"
)

func main() {
    
    genesisBlockChain := BLC.CreateBlockchainWithGenesisBlock()

    fmt.Println(genesisBlockChain)

    fmt.Println(genesisBlockChain.Blocks)

    fmt.Println(genesisBlockChain.Blocks[0])
}