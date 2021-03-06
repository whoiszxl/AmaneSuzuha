package BLC

import (
	"bytes"
	"log"
	"encoding/gob"
	"crypto/sha256"
)

// UTXO
type Transaction struct {

	//1. 交易hash
	TxHash []byte

	//2. 输入
	Vins []*TXInput

	//3. 输出
	Vouts []*TXOutput
}


//1. 创世区块创建时的Transaction
func NewCoinbaseTransaction(address string) *Transaction {

	//代表消费
	txInput := &TXInput{[]byte{}, -1, "coinbase...."}

	//代表收入
	txOutput := &TXOutput{10, address}

	//封装coinbase
	txCoinbase := &Transaction{ []byte{}, []*TXInput{txInput}, []*TXOutput{txOutput}}

	//设置hash值
	txCoinbase.HashTransaction()
	
	return txCoinbase
}

//将Transaction中的tx转为hash值
func (tx *Transaction) HashTransaction() {
	
	var result bytes.Buffer

	encoder := gob.NewEncoder(&result)

	err := encoder.Encode(tx)
	if err != nil {
		log.Panic(err)
	}

	hash := sha256.Sum256(result.Bytes())
	tx.TxHash = hash[:]
}