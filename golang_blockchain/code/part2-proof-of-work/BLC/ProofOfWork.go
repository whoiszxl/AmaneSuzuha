package BLC

import (
    "math/big"
    "bytes"
    "crypto/sha256"
)

//256位hash里面前面至少需要有8个0
const targetBit = 8


type ProofOfWork struct {
    Block *Block //当前要验证的区块
    target *big.Int //大数据存储
}

//数据拼接，返回字节数组
func (pow *ProofOfWork) prepareData(nonce int) []byte {
    data := bytes.Join(
        [][]byte{
            pow.Block.PrevBlockHash,
            pow.Block.Data,
            IntToHex(pow.Block.Timestamp),
            IntToHex(int64(targetBit)),
            IntToHex(int64(nonce)),
            IntToHex(int64(pow.Block.Height)),
        },
        []byte{},
    )
    
    return data
}

//校验工作量证明对象是否有效
func (proofOfWork *ProofOfWork) IsValid() bool {

	//1.proofOfWork.Block.Hash
    //2.proofOfWork.Target
    
	var hashInt big.Int
	// []byte 转 Int
	hashInt.SetBytes(proofOfWork.Block.Hash)


	// Cmp compares x and y and returns:
	//
	//   -1 if x <  y
	//    0 if x == y
	//   +1 if x >  y
	if proofOfWork.target.Cmp(&hashInt) == 1 {
		return true
	}

	return false
}

//工作量实际计算
func (proofOfWork *ProofOfWork) Run() ([]byte, int64) {

    nonce := 0

    var hashInt big.Int //用来存储新生成的hash
    var hash [32]byte

    for {
        //1. 将block的属性拼接成字节数组
        dataBytes := proofOfWork.prepareData(nonce)

        //2. 生成hash
        hash = sha256.Sum256(dataBytes)
        

        //3. 判断hash是否有效,判断hashInt是否小于Block里面的target
        hashInt.SetBytes(hash[:])
        if proofOfWork.target.Cmp(&hashInt) == 1 {
            break
        }

        nonce = nonce + 1
    }
    //fmt.Printf("\r%x",hash)
    return hash[:],int64(nonce)
}

//创建新的工作量证明对象
func NewProofOfWork(block *Block) *ProofOfWork {

    //1.创建一个初始值位1的target
    target := big.NewInt(1)

    //2. 左移256位 - targetBit位
    target.Lsh(target, 256 - targetBit)

    return &ProofOfWork{block, target}
}