package BLC

import (
    "time"
    "strconv"
    "bytes"
    "crypto/sha256"
)

//区块结构体
type Block struct {

    //1. 区块高度
    Height int64
    //2. 上一个区块的hash值
    PrevBlockHash []byte
    //3. 交易数据
    Data []byte
    //4. 时间戳
    Timestamp int64
    //5. Hash值
    Hash []byte
}

//求hash值
func (block *Block) SetHash() {

    //1. Height转[]byte
    heightBytes := IntToHex(block.Height)

    //2. 时间戳转[]byte
    timeString := strconv.FormatInt(block.Timestamp, 2)
    timeBytes := []byte(timeString)

    //3. 拼接所有属性
    blockBytes := bytes.Join([][]byte{heightBytes,block.PrevBlockHash,block.Data,timeBytes},[]byte{})

    //4. 生成hash
    hash := sha256.Sum256(blockBytes)

    block.Hash = hash[:]
}

//创建一个新的区块
func NewBlock(data string, height int64, prevBlockHash []byte) *Block {
    //1. 创建区块
    block := &Block{height, prevBlockHash, []byte(data), time.Now().Unix(), nil}

    //2. 设置hash
    block.SetHash()

    return block
}

func CreateGenesisBlock(data string) *Block {
    return NewBlock(data,1,[]byte{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
}