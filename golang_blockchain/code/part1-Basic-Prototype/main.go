package main

import (
    "fmt"
    "whoiszxl.com/AmaneSuzuha/golang_blockchain/code/part1-Basic-Prototype/BLC"
)

func main() {
    block := BLC.NewBlock("Genesis first block",1,[]byte{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
    fmt.Println(block)
}