package main

import (
    "fmt"
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part1-Basic-Prototype/BLC"
)

func main() {
    block := BLC.CreateGenesisBlock("Gensis first block")
    fmt.Println(block)
}