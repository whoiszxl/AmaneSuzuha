package BLC

import (
	"encoding/json"
    "bytes"
    "encoding/binary"
    "log"
)

func IntToHex(num int64) []byte {
    buff := new(bytes.Buffer)
    err := binary.Write(buff, binary.BigEndian, num)
    if err != nil {
        log.Panic(err)
    }

    return buff.Bytes()
}


//json转array
func JsonToArray(jsonString string) []string {
    var sArr []string
    if err := json.Unmarshal([]byte(jsonString), &sArr); err != nil {
        log.Panic(err)
    }
    return sArr
}