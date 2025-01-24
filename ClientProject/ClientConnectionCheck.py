# -*- coding: utf-8 -*-
"""
Created on Sat Jan 18 20:27:09 2025

@author: Saminur Islam
"""


# import os

# FILE_PATH = "keys.properties"  # Replace with the actual path to your properties file

# def clear_all_keys():
#     try:
#         # Resolve the absolute path to the file
#         user_home = os.path.expanduser("~")  # Get the user's home directory
#         absolute_path = os.path.join(user_home, FILE_PATH)

#         # Check if the file exists
#         if not os.path.exists(absolute_path):
#             print(f"File not found: {absolute_path}")
#             return

#         # Overwrite the file with nothing (clear its content)
#         with open(absolute_path, "w") as file:
#             pass  # Write nothing to the file

#         print(f"All keys have been removed from {absolute_path}")
#     except Exception as e:
#         print(f"Error clearing keys from file: {e}")
        
# keys_to_delete = ["DeveloperID", "ClientKey", "CLientSecret"]  # Replace with keys you want to delete
# clear_all_keys()       
import json
import socket
import base64
import os
import requests
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
from email import encoders
from uuid import getnode as get_mac
from time import sleep
from datetime import datetime
from Crypto.Cipher import AES, PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Random import get_random_bytes
from Crypto.Util.Padding import unpad
from Crypto.Random import random

# Configuration
SERVER_HOST = "xxxx.xxx.xxx.xx"
SERVER_PORT = 5002
CHECK_INTERVAL = 86400  # 24 hours in seconds
EMAIL_ADDRESSES = ["ab@edu", "ac@edu", "ae@edu"]
SMTP_SERVER = "smtp.example.com"
SMTP_PORT = 587
SMTP_USERNAME = "your_email@example.com"
SMTP_PASSWORD = "your_email_password"

def generate_rsa_key_pair():
    # Generate an RSA key pair
    # random.seed(42)  # Set the seed for the random generator

    # Generate a 1024-bit RSA key pair
    key = RSA.generate(1024,e=65537)

    # Extract the public and private keys
    private_key = key.export_key()  # Export the private key in PEM format
    public_key = key.publickey().export_key()  
    return private_key, public_key

def connect_to_server():
    try:
        private_key, public_key = generate_rsa_key_pair()

        print("Generated RSA Public Key (public_key):")
        # print(public_key)

        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            client_socket.settimeout(20)  # Set a timeout for socket operations
            client_socket.connect((SERVER_HOST, SERVER_PORT))

            # Send the PEM public key directly
            client_socket.send(public_key)
            print("Public key sent to the server.")

            # Receive encrypted AES key and related data
            encrypted_aes_key = client_socket.recv(128)
            if not encrypted_aes_key:
                raise ValueError("Received empty AES key from server.")

            print(f"Encrypted AES Key Length: {len(encrypted_aes_key)}")
            print(f"Encrypted AES Key: {encrypted_aes_key}")

            # Additional IVs and encrypted data
            iv1 = client_socket.recv(16)
            client_id_encrypted = client_socket.recv(16)
            iv2 = client_socket.recv(16)
            client_secret_encrypted = client_socket.recv(16)

            if not (iv1 and client_id_encrypted and iv2 and client_secret_encrypted):
                raise ValueError("Received incomplete data from server.")

            print("Successfully received all encrypted data from the server.")

            # Decrypt AES key using RSA private key
            rsa_cipher = PKCS1_OAEP.new(RSA.import_key(private_key))
            aes_key = rsa_cipher.decrypt(encrypted_aes_key)
            print("Decrypted AES Key:", aes_key)

            # Decrypt Client ID
            aes_cipher = AES.new(aes_key, AES.MODE_CBC, iv1)
            client_id = unpad(aes_cipher.decrypt(client_id_encrypted), AES.block_size).decode()
            print("Decrypted Client ID:", client_id)

            # Decrypt Client Secret
            aes_cipher = AES.new(aes_key, AES.MODE_CBC, iv2)
            client_secret = unpad(aes_cipher.decrypt(client_secret_encrypted), AES.block_size).decode()
            print("Decrypted Client Secret:", client_secret)

            return aes_key, client_secret, client_id

    except Exception as e:
        print(f"Error during server connection: {e}")
        return None, None, None

# Function to send a JSON file securely to the server
def send_file_to_server(aes_key, client_secret, client_id):
    try:
        ip_address = socket.gethostbyname(socket.gethostname())
        mac_address = get_mac()

        data = {
            "sequentialEventData": [],
            "errorLogList": [],
            "IPAddress": ip_address,
            "MACAddress": mac_address
        }

        json_data = json.dumps(data).encode()

        # Encrypt the file using AES key
        cipher = AES.new(aes_key, AES.MODE_CBC)
        iv = cipher.iv
        encrypted_data = cipher.encrypt(json_data.ljust((len(json_data) // 16 + 1) * 16))

        headers = {
            "Client-Id": client_id,
            "Client-Secret": base64.b64encode(client_secret.encode()).decode(),
            "IV": base64.b64encode(iv).decode(),
        }

        response = requests.post(f"http://{SERVER_HOST}:{SERVER_PORT}/upload", headers=headers, data=encrypted_data)

        if response.status_code == 200:
            print("File sent successfully to the server.")
        else:
            print(f"Failed to send file. Server responded with status code {response.status_code}")

    except Exception as e:
        print(f"Error during file transmission: {e}")

# Function to send email in case of failure
def send_email(subject, body, recipients):
    try:
        msg = MIMEMultipart()
        msg['From'] = SMTP_USERNAME
        msg['To'] = ", ".join(recipients)
        msg['Subject'] = subject
        msg.attach(MIMEText(body, 'plain'))

        server = smtplib.SMTP(SMTP_SERVER, SMTP_PORT)
        server.starttls()
        server.login(SMTP_USERNAME, SMTP_PASSWORD)
        server.sendmail(SMTP_USERNAME, recipients, msg.as_string())
        server.quit()
        print("Email sent successfully.")
    except Exception as e:
        print(f"Failed to send email: {e}")

# Main script to perform daily checks
def main():
    while True:
        print(f"[{datetime.now()}] Starting server connection check.")

        aes_key, client_secret, client_id = connect_to_server()

        if aes_key and client_secret and client_id:
            send_file_to_server(aes_key, client_secret, client_id)
        else:
            subject = "Client-Server Connection Failure"
            body = "Failed to establish a connection with the server or authenticate. Immediate attention is required."
            # send_email(subject, body, EMAIL_ADDRESSES)

        print(f"[{datetime.now()}] Check completed. Next check in 24 hours.")
        # sleep(CHECK_INTERVAL)

if __name__ == "__main__":
    main()
