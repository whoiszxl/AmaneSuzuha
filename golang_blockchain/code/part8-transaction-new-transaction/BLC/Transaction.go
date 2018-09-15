package BLC

import (
	"fmt"
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
	txInput := &TXInput{[]byte{}, -1, "coinbase transaction"}

	//代表收入
	txOutput := &TXOutput{10, address}

	//封装coinbase
	txCoinbase := &Transaction{ []byte{}, []*TXInput{txInput}, []*TXOutput{txOutput}}

	//设置hash值
	txCoinbase.HashTransaction()
	
	return txCoinbase
}

//2. 转账时候产生的Transaction
func NewSimpleTransaction(from string, to string, amount int) *Transaction {


	//返回from这个地址所有的未花费交易输出所对应的Transaction
	unSpentTx := UnSpentTransationsWithAddress(from)

	fmt.Println(unSpentTx)

	// var txInputs []*TXInput
	// var txOutputs []*TXOutput

	// //代表消费
	// bytes ,_ := hex.DecodeString("cea12d33b2e7083221bf3401764fb661fd6c34fab50f5460e77628c42ca0e92b")
	// txInput := &TXInput{bytes, 0, from}

	// //消费
	// txInputs = append(txInputs, txInput)

	// //转账
	// txOutput := &TXOutput{int64(amount), to}
	// txOutputs = append(txOutputs, txOutput)

	// //找零
	// txOutput = &TXOutput{ 10 - int64(amount), from }
	// txOutputs = append(txOutputs, txOutput)

	// tx := &Transaction{ []byte{}, txInputs, txOutputs }

	// //设置hash
	// tx.HashTransaction()

	return nil
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

