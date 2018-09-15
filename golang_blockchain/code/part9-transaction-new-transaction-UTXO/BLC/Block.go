package BLC

import (
    "time"
    "bytes"
    "fmt"
    "encoding/gob"
    "log"
    "crypto/sha256"
)

//区块结构体
type Block struct {

    //1. 区块高度
    Height int64
    //2. 上一个区块的hash值
    PrevBlockHash []byte
    //3. 交易数据
    Txs []*Transaction
    //4. 时间戳
    Timestamp int64
    //5. Hash值
    Hash []byte
    //6. Nonce工作量证明随机数
    Nonce int64
}


//将Txs转换成[]byte
func (block *Block) HashTransactions() []byte {

    var txHashes [][]byte
    var txHash [32]byte

    for _, tx := range block.Txs {
        //将交易数组里的交易遍历，并将每个交易的hash添加到txHashes中
        txHashes = append(txHashes, tx.TxHash)
    }

    txHash = sha256.Sum256(bytes.Join(txHashes, []byte{}))
    return txHash[:]
}


//序列化区块
func (block *Block) Serialize() []byte {
    var result bytes.Buffer

    encoder := gob.NewEncoder(&result)
    err := encoder.Encode(block)
    if err != nil {
        log.Panic(err)
    }
    return result.Bytes()
}


//反序列化区块
func DeserializeBlock(blockBytes []byte) *Block {
    var block Block

    decoder := gob.NewDecoder(bytes.NewReader(blockBytes))
    err := decoder.Decode(&block)
    if err != nil {
        log.Panic(err)
    }
    return &block
}


//创建一个新的区块
func NewBlock(txs []*Transaction, height int64, prevBlockHash []byte) *Block {
    //1. 创建区块
    block := &Block{height, prevBlockHash, txs, time.Now().Unix(), nil, 0}

    //2. 调用工作量证明的方法并且返回有效的Hash和Nonce
    pow := NewProofOfWork(block)
    // 挖矿验证
    hash, nonce := pow.Run()
    
    //3. 设置哈希和工作量随机数
    block.Hash = hash[:]
    block.Nonce = nonce

    fmt.Println()

    return block
}

func CreateGenesisBlock(txs []*Transaction) *Block {
    return NewBlock(txs,1,[]byte{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
}