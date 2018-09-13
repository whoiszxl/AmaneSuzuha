package main

import (
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part4-persistence/BLC"
)

func main() {
    
    blockchain := BLC.CreateBlockchainWithGenesisBlock()
    defer blockchain.DB.Close()

    blockchain.AddBlockToBlockchain("hello 1")
}