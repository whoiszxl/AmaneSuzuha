package main

import (
    "fmt"
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part2-proof-of-work/BLC"
)

func main() {
    
    blockchain := BLC.CreateBlockchainWithGenesisBlock()

    // 新区块
    blockchain.AddBlockToBlockchain("Send 100RMB To jiangxi",blockchain.Blocks[len(blockchain.Blocks) - 1].Height + 1,blockchain.Blocks[len(blockchain.Blocks) - 1].Hash)

    blockchain.AddBlockToBlockchain("Send 200RMB To chenhuixian",blockchain.Blocks[len(blockchain.Blocks) - 1].Height + 1,blockchain.Blocks[len(blockchain.Blocks) - 1].Hash)

    blockchain.AddBlockToBlockchain("Send 300RMB To wangfei",blockchain.Blocks[len(blockchain.Blocks) - 1].Height + 1,blockchain.Blocks[len(blockchain.Blocks) - 1].Hash)

    blockchain.AddBlockToBlockchain("Send 50RMB To flower",blockchain.Blocks[len(blockchain.Blocks) - 1].Height + 1,blockchain.Blocks[len(blockchain.Blocks) - 1].Hash)

    fmt.Println(blockchain)

    fmt.Println(blockchain.Blocks)
}