# -*- coding: utf-8 -*-
# @Time    : 2018/5/13
# @Author  : whoiszxl
# @File    : blockchain.py

__author__ = 'whoiszxl'

# {
#     "index": 0, # 下标
#     "timestamp": "", # 时间戳
#     "transactions" [
#         {
#             "sender": "", # 发送者
#             "recipient": "", # 接收者
#             "amount": 5 # 发送金额
#         }
#     ], # 交易集合
#     "proof": "", # 工作量证明
#     "previous_hash": "" # 上一个区块的哈希
# }
import hashlib
import json
from time import time
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse
from uuid import uuid4

import requests
from flask import Flask, jsonify, request

class Blockchain:
    def __init__(self):
        self.chain = []
        self.currrent_transactions = []
        self.nodes = set()

        # 创建一个创世区块
        self.new_block(proof=100, previous_hash=1)


    def register_node(self, address: str) -> None:
        """
        添加一个节点到节点列表上
        :param address: <str> 节点地址. Eg. 'http://0.0.0.0:5000'
        :return: None
        """
        parsed_url = urlparse(address)
        self.nodes.add(parsed_url.netloc)

    def valid_chain(self, chain: List[Dict[str, Any]]) -> bool:
        """
        校验区块链是否有效

        :param chain: A blockchain
        :return: True if valid, False if not
        遍历整个传入的区块链，将第一个和第零个依次去对比hash值，工作量证明
        """
        
        last_block = chain[0]
        current_index = 1

        while current_index < len(chain):
            block = chain[current_index]
            print(f'{last_block}')
            print(f'{block}')
            print("\n-----------\n")

            if block['previous_hash'] != self.hash(last_block):
                return False
            
            if not self.valid_proof(last_block['proof'], block['proof']):
                return False

            last_block = block
            current_index += 1

        return True

    def resolve_conflicts(self) -> bool:
        """
        共识算法解决冲突
        使用网络中最长的链.

        :return:  如果链被取代返回 True, 否则为False
        """

        neighbours = self.nodes
        new_chain = None

        # We're only looking for chains longer than ours
        max_length = len(self.chain)

        # Grab and verify the chains from all the nodes in our network
        for node in neighbours:
            response = requests.get(f'http://{node}/chain')

            if response.status_code == 200:
                length = response.json()['length']
                chain = response.json()['chain']

                # Check if the length is longer and the chain is valid
                if length > max_length and self.valid_chain(chain):
                    max_length = length
                    new_chain = chain

        # Replace our chain if we discovered a new, valid chain longer than ours
        if new_chain:
            self.chain = new_chain
            return True

        return False

    def new_block(self, proof, previous_hash = None):
        block = {
            'index': len(self.chain) + 1,
            'timestamp': time(),
            'transactions': self.currrent_transactions,
            'proof': proof,
            'previous_hash': previous_hash or self.hash(self.last_block)
        }

        self.currrent_transactions = []
        self.chain.append(block)

        return block
    
    def new_transaction(self, sender, recipient, amount) -> int:
        self.currrent_transactions.append(
            {
                'sender': sender,
                'recipient': recipient,
                'amount': amount
            }
        )

        return self.last_block['index'] + 1
    
    @staticmethod
    def hash(block):
        block_string = json.dumps(block, sort_keys=True).encode()
        return hashlib.sha256(block_string).hexdigest()
    
    @property
    def last_block(self):

        return self.chain[-1]

    def proof_of_work(self, last_proof: int) ->int:
        proof = 0

        while self.valid_proof(last_proof, proof) is False:
            proof += 1
        print(proof)
        return proof

    def valid_proof(self, last_proof, proof: int) -> bool:
        guess = f'{last_proof}{proof}'.encode()
        guess_hash = hashlib.sha256(guess).hexdigest()
        print(guess_hash)
        return guess_hash[0:4] == "0000"


app = Flask(__name__)
# 给节点创建一个唯一地址
node_identifier = str(uuid4()).replace('-', '')

# 初始化一个创世区块
blockchain = Blockchain()

@app.route('/transactions/new', methods=['POST'])
def new_transaction():
    # 获取到请求中的参数
    values = request.get_json()
    if values is None:
        return 'Missing values', 400
    # 必须包含如下的字段
    required = ['sender', 'recipient', 'amount']
    if not all(k in values for k in required):
        return 'Missing values', 400

    # 创建一个交易
    index = blockchain.new_transaction(values['sender'], values['recipient'], values['amount'])

    # 返回提示
    response = {'message': f'交易将要添加到块中了 {index}'}
    return jsonify(response), 201



@app.route('/index', methods=['GET'])
def index():
    return "hello index"



@app.route('/mine', methods=['GET'])
def mine():
    # 获取到上一个区块的工作量证明
    last_block = blockchain.last_block
    last_proof = last_block['proof']
    # 通过上一个区块的证明再去进行计算出一个新的工作证明
    proof = blockchain.proof_of_work(last_proof)

    # 给挖矿节点一点微小的奖励
    # 发送者为0意味着这是新挖出来的币
    blockchain.new_transaction(
        sender="0",
        recipient=node_identifier,
        amount=10,
    )
    # 通过工作证明创建一个新的区块
    block = blockchain.new_block(proof)
    response = {
        'message': "New Block Forged",
        'index': block['index'],
        'transactions': block['transactions'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash'],
    }
    return jsonify(response), 200




@app.route('/chain', methods=['GET'])
def full_chain():
    response = {
        'chain': blockchain.chain,
        'length': len(blockchain.chain)
    }

    return jsonify(response), 200



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
