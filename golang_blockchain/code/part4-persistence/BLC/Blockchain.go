package BLC

import (
    "github.com/boltdb/bolt"
    "log"
)

//数据库名称
const dbName = "blockchain.db"
//表名
const blockTableName = "blocks"


//区块链结构体
type Blockchain struct {
    Tip []byte //最新的区块的Hash
    DB *bolt.DB
}


//添加新的区块到区块链中
// func (blc *Blockchain) AddBlockToBlockchain(data string, height int64, preHash []byte) {
//     //创建新区块
//     newBlock := NewBlock(data, height, preHash) 
//     //往公链添加区块
//     blc.Blocks = append(blc.Blocks, newBlock)
// }


//创建带有创世区块的区块链
func CreateBlockchainWithGenesisBlock() *Blockchain {

    //1. 创建or打开数据库
    db, err := bolt.Open(dbName, 0600, nil)
    if err != nil {
        log.Fatal(err)
    }
    
    var blockHash []byte

    err = db.Update(func(tx *bolt.Tx) error{
        //创建数据库
        b, err := tx.CreateBucket([]byte(blockTableName))

        if err != nil {
            log.Panic(err)
        }

        if b == nil {
            //创建创世区块
            genesisBlock := CreateGenesisBlock("Genesis frist block...")
            
            //将创世区块存储到表中
            err := b.Put(genesisBlock.Hash,genesisBlock.Serialize())
            if err != nil {
                log.Panic(err)
            }
            
            // 存储最新的区块的hash
            err = b.Put([]byte("l"),genesisBlock.Hash)
            if err != nil {
                log.Panic(err)
            }

            blockHash = genesisBlock.Hash
        }

        return nil
    })

    //返回区块链对象
    return &Blockchain{blockHash,db}
}