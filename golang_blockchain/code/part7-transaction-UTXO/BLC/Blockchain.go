package BLC

import (
	"math/big"
    "github.com/boltdb/bolt"
    "log"
    "fmt"
    "time"
    "os"
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

//区块链初始化迭代器的方法
func (blockchain *Blockchain) Iterator() *BlockchainIterator {
    return &BlockchainIterator{blockchain.Tip, blockchain.DB}
}

// 判断数据库是否存在
func dbExists() bool {
	if _, err := os.Stat(dbName); os.IsNotExist(err) {
		return false
	}
	return true
}


//遍历输出所有的区块
func (blc *Blockchain) PrintChain() {

    blockchainIterator := blc.Iterator()

    for {
        block := blockchainIterator.Next()

        fmt.Printf("Height：%d\n",block.Height)
		fmt.Printf("PrevBlockHash：%x\n",block.PrevBlockHash)
		fmt.Printf("Timestamp：%s\n",time.Unix(block.Timestamp, 0).Format("2006-01-02 03:04:05 PM"))
		fmt.Printf("Hash：%x\n",block.Hash)
        fmt.Printf("Nonce：%d\n",block.Nonce)
		fmt.Printf("Txs::")
		//循环打印交易输入和输出
		for _,tx := range block.Txs {
			fmt.Printf("%x\n",tx.TxHash)
			fmt.Println("Vins:")
			for _,in := range tx.Vins  {
				fmt.Printf("%x\n",in.TxHash)
				fmt.Printf("%d\n",in.Vout)
				fmt.Printf("%s\n",in.ScriptSig)
			}

			fmt.Println("Vouts:")
			for _,out := range tx.Vouts  {
				fmt.Println(out.Value)
				fmt.Println(out.ScriptPubKey)
			}
		}
		fmt.Println("-----------------------------------------------")

        var hashInt big.Int
        hashInt.SetBytes(block.PrevBlockHash)

        // 【-1 if x < y】 【0 if x == y】 【+1 if x > y】
        if big.NewInt(0).Cmp(&hashInt) == 0 {
            break;
        }
    }

}

// 增加区块到区块链里面
func (blc *Blockchain) AddBlockToBlockchain(txs []*Transaction) {

	err := blc.DB.Update(func(tx *bolt.Tx) error{

		//1. 获取表
		b := tx.Bucket([]byte(blockTableName))
		//2. 创建新区块
		if b != nil {

			// 先获取最新区块
			blockBytes := b.Get(blc.Tip)
			// 反序列化
			block := DeserializeBlock(blockBytes)

			//3. 将区块序列化并且存储到数据库中
			newBlock := NewBlock(txs,block.Height + 1,block.Hash)
			err := b.Put(newBlock.Hash,newBlock.Serialize())
			if err != nil {
				log.Panic(err)
			}
			//4. 更新数据库里面"l"对应的hash
			err = b.Put([]byte("l"),newBlock.Hash)
			if err != nil {
				log.Panic(err)
			}
			//5. 更新blockchain的Tip
			blc.Tip = newBlock.Hash
		}

		return nil
	})

	if err != nil {
		log.Panic(err)
	}
}


//创建带有创世区块的区块链
func CreateBlockchainWithGenesisBlock(address string) {

    //判断数据库是否存在
    if dbExists() {
        fmt.Println("创世区块链已经存在......")
        os.Exit(1)
    }

    fmt.Println("正在创建创世区块.......")

    //创建或打开数据库
    db, err := bolt.Open(dbName, 0600, nil)
    if err != nil {
        log.Fatal(err)
    }

    err = db.Update(func(tx *bolt.Tx) error{

        // 创建数据库表
		b, err := tx.CreateBucket([]byte(blockTableName))

		if err != nil {
			log.Panic(err)
		}

		if b != nil {
			//创建一个coinbase交易
			txCoinbase := NewCoinbaseTransaction(address)

			// 创建创世区块
			genesisBlock := CreateGenesisBlock([]*Transaction{txCoinbase})
			// 将创世区块存储到表中
			err := b.Put(genesisBlock.Hash, genesisBlock.Serialize())
			if err != nil {
				log.Panic(err)
			}

			// 存储最新的区块的hash
			err = b.Put([]byte("l"), genesisBlock.Hash)
			if err != nil {
				log.Panic(err)
			}
		}

		return nil
    })
}

// 返回Blockchain对象
func BlockchainObject() *Blockchain {

    //打开数据库
	db, err := bolt.Open(dbName, 0600, nil)
	if err != nil {
		log.Fatal(err)
	}

	var tip []byte

	err = db.View(func(tx *bolt.Tx) error {

        //获取bucket表读取到最新区块的hash值
		b := tx.Bucket([]byte(blockTableName))

		if b != nil {
			// 读取最新区块的Hash
			tip = b.Get([]byte("l"))

		}

		return nil
	})

    //返回一个带有最新区块hash的区块链对象
	return &Blockchain{tip,db}
}


//挖新的区块
func (blockchain *Blockchain) MineNewBlock(from []string, to []string, amount []string) {
	fmt.Println("---------开始挖矿--------")
	fmt.Println(from)
	fmt.Println(to)
	fmt.Println(amount)

	//1. 通过相关算法创建Transaction数组
	var txs []*Transaction

	var block *Block
	blockchain.DB.View(func(tx *bolt.Tx) error{
		b := tx.Bucket([]byte(blockTableName))
		if b != nil {
			hash := b.Get([]byte("l"))
			blockBytes := b.Get(hash)
			block = DeserializeBlock(blockBytes)
		}
		return nil
	})

	//2. 创建新的区块
	block = NewBlock(txs, block.Height+1, block.Hash)

	//3. 将新区块存储到数据库
	blockchain.DB.Update(func(tx *bolt.Tx) error{
		b := tx.Bucket([]byte(blockTableName))
		if b != nil {
			b.Put(block.Hash, block.Serialize())
			b.Put([]byte("l"), block.Hash)
			blockchain.Tip = block.Hash
		}

		return nil
	})
}