package main

import (
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part4-persistence/BLC"
)

func main() {
    
    blockchain := BLC.CreateBlockchainWithGenesisBlock()
    defer blockchain.DB.Close()

    blockchain.AddBlockToBlockchain("hello rose one")
    blockchain.AddBlockToBlockchain("hello jack two")
    blockchain.AddBlockToBlockchain("hello hins three")

    blockchain.PrintChain()
}