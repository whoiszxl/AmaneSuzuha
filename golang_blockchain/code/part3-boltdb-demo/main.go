package main

import (
    "fmt"
    "github.com/boltdb/bolt"
    "log"
)

func main() {
    
    //创建or打开数据库
    db, err := bolt.Open("my.db", 0600, nil)
    if err != nil {
        log.Fatal(err)
    }

    defer db.Close()

    //创建表
    // err = db.Update(func(tx *bolt.Tx) error {
    //     //创建BlockBucket表
    //     b := tx.Bucket([]byte("BlockBucket"))
        
    //     // 往表里面存储数据
    //     if b != nil {
    //         err := b.Put([]byte("l"),[]byte("helllo world"))
    //             if err != nil {
    //             log.Panic("数据存储失败......")
    //         }
    //     }
        

    //     // 返回nil，以便数据库处理相应操作
    //     return nil
    // })

    // //更新失败
    // if err != nil {
    //     log.Panic(err)
    // }

    // 查看数据

    err = db.View(func(tx *bolt.Tx) error {

        // 获取BlockBucket表对象
        b := tx.Bucket([]byte("BlockBucket"))

        // 往表里面存储数据
        if b != nil {
            data := b.Get([]byte("l"))
            fmt.Printf("%s\n",data)
        }

        // 返回nil，以便数据库处理相应操作
        return nil
    })

}