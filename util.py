from Crypto.Cipher import AES,PKCS1_OAEP
from Crypto.Random import get_random_bytes
from Crypto.PublicKey import RSA
import socket
import os
import string
import random
import binascii
import sys
import datetime
import json


HOST='lurch.csc.ncsu.edu'
def get_random_string(n):
    res = ''.join(random.choices(string.ascii_uppercase +
                                 string.digits, k=n))
    return res

def gen_aes_key(rsa_public):

    recipient_key = RSA.import_key(rsa_public)
    #print(rsa_public)
    aes_key = binascii.hexlify(os.urandom(16))
    cipher_rsa = PKCS1_OAEP.new(recipient_key)
    enc_aes_key = cipher_rsa.encrypt(aes_key)
    return aes_key,enc_aes_key
def aes_encrpt_and_send(conn,msg,aes_key):
    cipher = AES.new(aes_key, AES.MODE_CBC)
    ciphertext = cipher.encrypt(msg.encode())
    iv = cipher.iv
    print("iv",iv)
    print(ciphertext)
    conn.send(iv)
    conn.send(ciphertext)
def aes_decrpt(emsg,aes_key,iv):
    cipha_aes = AES.new(aes_key, AES.MODE_CBC, iv)
    msg = cipha_aes.decrypt(emsg)
    return msg
def aes_recv_and_decrpt(conn,aes_key,emsg_size):
    iv = conn.recv(16)
    #print('iv', iv)
    enc_msg = conn.recv(emsg_size)
    msg=aes_decrpt(enc_msg,aes_key,iv)
    return msg.decode()

def check_secret(aes_key,client_secret,input,iv):
    cipher = AES.new(aes_key, AES.MODE_CBC,iv)
    data = cipher.decrypt(input)
    if data == client_secret:
        return True
    else:
        return False

    
