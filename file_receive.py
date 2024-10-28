import socket
import tqdm
import os
from models import *
import json
import datetime
from base64 import b64decode
from base64 import b64encode

from http.server import BaseHTTPRequestHandler, HTTPServer
import logging

logger=logging.getLogger()
logger.setLevel(logging.INFO)
formatter=logging.Formatter(fmt=" %(message)s")
handler = logging.StreamHandler(stream=sys.stdout)
handler.setFormatter(formatter)
logger.addHandler(handler)

class S(BaseHTTPRequestHandler):
    def _set_response(self,number=501):
        self.send_response(number)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        self._set_response()
        #self.wfile.write("GET request for {}".format(self.path).encode('utf-8'))

    def do_POST(self):
        content_length = int(self.headers['Content-Length']) # <--- Gets the size of data
        body = self.rfile.read(content_length) # <--- Gets the data itself
        client_id = self.headers['Client-Id']
        filename = self.headers['Filename']
        encrpted_client_secret = b64decode(self.headers['Client-Secret'])
        iv1 =self.headers['iv1']
        iv2=self.headers['iv2']
        iv1=b64decode(iv1)
        iv2=b64decode(iv2)
        print(iv2,encrpted_client_secret)

        logging.info(self.headers)
        logging.info(client_id)
        c=find_client(client_id=client_id)
        #c = Client.find(Client.client_id == client_id).all()[0]
        logging.info(c.aes_key)
        logging.info(c.client_secret)
        #logging.info(self.headers)
        #print(aes_decrpt(emsg=encrpted_client_secret,aes_key=c.aes_key,iv=iv).decode())
        if c and c.check_aes(encrpted_client_secret,iv2):
            path='/usr/local/share/data/today/'+filename
            logging.info(path)
            with open(path, "ab") as file:
                file.write(aes_decrpt(b64decode(body),c.aes_key.encode(),iv1))

            logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\n",
                str(self.path), str(self.headers))
            self._set_response(200)

        else:
            self._set_response(501)
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))

def run(server_class=HTTPServer, handler_class=S, port=5001):
    logging.basicConfig(level=logging.INFO)
    server_address = (HOST, port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    logging.info('Stopping httpd...\n')
run()
