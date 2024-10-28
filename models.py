#from redis_om import Migrator,get_redis_connection,HashModel,Field
import redis
from typing import List
import datetime
from Crypto.Cipher import AES
from util import  *
r = redis.Redis(host='localhost', port=6379, decode_responses=True)
class Client():
    def __init__(self,client_id,client_secret,aes_key):
        self.client_id=client_id
        self.client_secret=client_secret
        self.aes_key=aes_key

    def save(self):
        print(self.client_secret)
        print(self.client_id)
        print(self.aes_key)
        dic={'client_secret':self.client_secret,'aes_key':self.aes_key}
        r.hmset(self.client_id,dic)
        #r.hmset(self.client_id,dic)
        #r.hset(self.client_id,'client_secret',self.client_secret)
        #r.hset(self.client,'aes_key',self.aes_key)

    def check_aes(self,input,iv):
        print(self.aes_key,type(self.aes_key))
        cipher = AES.new(self.aes_key.encode(),AES.MODE_CBC,iv)
        data = cipher.decrypt(input)
        print('decrpted')
        print(data)
        if data.decode() == self.client_secret:
            return True
        else:
            print("correct:")
            print(self.client_secret)
            print("received:")
            print(data.decode())
            return False
    def decrpt_msg(self,input,iv):
        return aes_decrpt(input,self.aes_key,iv)
    def check_duplicate(self):
        return r.exists(self.client_id)
def find_client(client_id):
    temp=r.hgetall(client_id)
    try:
        client_secret=temp['client_secret']
        aes_key=temp['aes_key']
        #print(aes_key)
        return Client(client_id=client_id,client_secret=client_secret,aes_key=aes_key)
    except:
        return False
