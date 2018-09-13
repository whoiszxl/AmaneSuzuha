package BLC

import (
	"github.com/boltdb/bolt"
	"log"
)

//迭代器结构
type BlockchainIterator struct {
	CurrentHash []byte
	DB  *bolt.DB
}

//迭代器获取下一个区块的方法
func (blockchainIterator *BlockchainIterator) Next() *Block {

	var block *Block

	err := blockchainIterator.DB.View(func(tx *bolt.Tx) error{

		b := tx.Bucket([]byte(blockTableName))

		if b != nil {
			currentBloclBytes := b.Get(blockchainIterator.CurrentHash)
			//  获取到当前迭代器里面的currentHash所对应的区块
			block = DeserializeBlock(currentBloclBytes)

			// 更新迭代器里面CurrentHash
			blockchainIterator.CurrentHash = block.PrevBlockHash
		}

		return nil
	})

	if err != nil {
		log.Panic(err)
	}


	return block

}