import sys

import logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)
formatter = logging.Formatter(fmt=" %(message)s")

# this logs to stdout and I think it is flushed immediately
handler = logging.StreamHandler(stream=sys.stdout)
handler.setFormatter(formatter)
logger.addHandler(handler)

#fh = logging.FileHandler('./log/myLog.log')
#fh.setLevel(logging.DEBUG) # or any level you want
#logger.addHandler(fh)
from util import *
from models import *

logger.info('start')
BUFFER_SIZE = 4096
SEPARATOR = ','

s = socket.socket()
#HOST = '0.0.0.0'
PORT = 5002

# bind the socket to our local address
s.bind((HOST, PORT))

s.listen(5)
while True:
    conn, address = s.accept()
    try:
        rsa_public = conn.recv(BUFFER_SIZE)
        rsa_public = conn.recv(3)
        #print(rsa_public)
        rsa_public = conn.recv(BUFFER_SIZE)
        logger.info("received rsa key:")
        logger.info(rsa_public)
        aes_key, enc_aes_key=gen_aes_key(rsa_public)
        logger.info("genrated aes key:")
        logger.info(aes_key)
        logger.info("sending encryted aes key:")
        logger.info(enc_aes_key)
        logger.info(len(enc_aes_key))
        conn.send(enc_aes_key)

        client_id=get_random_string(16)
        client_secret=get_random_string(16)
        cle= Client(client_id=client_id,client_secret=client_secret,aes_key=aes_key)
        while(cle.check_duplicate()):
            client_id = get_random_string(16)
            client_secret = get_random_string(16)
            cle = Client(client_id=client_id, client_secret=client_secret, aes_key=aes_key)
        cle.save()
        del cle
        logger.info('client id:'+ client_id)
        logger.info('client secret:'+ client_secret)


        aes_encrpt_and_send(conn,client_id,aes_key)
        aes_encrpt_and_send(conn, client_secret, aes_key)
        conn.close()
    except Exception as e:
        print(e)
        conn.close()

