from fastapi import FastAPI, Depends, HTTPException, status, Request, Header
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi.responses import JSONResponse
from Crypto.Cipher import AES
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel
from datetime import datetime, timedelta
from typing import Optional, List
import os
from fastapi.responses import FileResponse
from util import gen_aes_key, get_random_string
from models import Client, find_client
from Crypto.Util.Padding import pad,unpad

from Crypto.Random import get_random_bytes
import base64
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
# Initialize the app
app = FastAPI()

# Password hashing context
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Secret key to encode and decode JWT tokens
SECRET_KEY = os.getenv("SECRET_KEY", "your_secret_key")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# OAuth2 scheme for getting the current user
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# In-memory user store (for demonstration)
# In production, use a database
fake_users_db = {}
# $2b$12$XMTEzxbrUvt.AptFeG3VduLJDtKfNXJB.NePoXoX0JAYMRpckn58.
#default_user = UserInDB( username="admin", hashed_password=get_password_hash("admin") ) 
#fake_users_db[default_user.username] = default_user.dict()
# Pydantic models for data validation
class User(BaseModel):
    username: str

class UserInDB(User):
    hashed_password: str

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    username: Optional[str] = None

# Utility functions
def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

def authenticate_user(fake_db, username: str, password: str):
    user = fake_db.get(username)
    #print(user)
    #print(password)
    if user and verify_password(password, user['hashed_password']):
        return user
    return None

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def get_user(fake_db, username: str):
    if username in fake_db:
        user_dict = fake_db[username]
        return UserInDB(**user_dict)

def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
        token_data = TokenData(username=username)
    except JWTError:
        raise credentials_exception
    user = get_user(fake_users_db, username=token_data.username)
    if user is None:
        raise credentials_exception
    return user



default_user = UserInDB( username="admin", hashed_password=get_password_hash("Coding@Scale") ) 
fake_users_db[default_user.username] = default_user.dict()

'''
# Registration route
@app.post("/register", response_model=User)
def register(user: UserInDB):
    if user.username in fake_users_db:
        raise HTTPException(
            status_code=400,
            detail="Username already registered",
        )
    user.hashed_password = get_password_hash(user.hashed_password)
    fake_users_db[user.username] = user.dict()
    return user

# Token generation (login)
'''
@app.post("/token", response_model=Token)
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    user = authenticate_user(fake_users_db, form_data.username, form_data.password)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user['username']}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}

# List files (requires authentication)
@app.get("/files", dependencies=[Depends(get_current_user)])
def list_files() -> List[str]:
    FILES_DIR = "/usr/local/share/data/sending"
    try:
        files = os.listdir(FILES_DIR)
        files.sort()
        return files
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Directory not found")

# Download file (requires authentication)
@app.get("/download/{filename}")
def download_file(filename: str,token: str = Depends(oauth2_scheme)):
    FILES_DIR = "/usr/local/share/data/sending"
    file_path = os.path.join(FILES_DIR, filename)
    user = get_current_user(token)
    if os.path.isfile(file_path):
        return FileResponse(path=file_path, filename=filename)
    else:
        raise HTTPException(status_code=404, detail="File not found")

def aes_encrypt(data, key):
    """
    Encrypts data using AES in CBC mode with PKCS#7 padding.
    `key` is expected to be a hex string.
    """
    if isinstance(data, str):
        data = data.encode()
    data = pad(data,AES.block_size)
    iv= get_random_bytes(16)  # Convert hex key to bytes
    cipher = AES.new(key, AES.MODE_CBC,iv)
    encrypted = cipher.encrypt(data)
    return iv, encrypted


@app.post("/register")
async def register(request: Request):
    """
    Registration endpoint.

    Expects JSON with:
      - public_key: Base64-encoded RSA public key.

    Returns JSON containing:
      - enc_aes: Base64-encoded AES key encrypted with the provided RSA public key.
      - cid: The client ID.
      - cs: Base64-encoded AES-encrypted client secret.
      - cs_iv: Base64-encoded IV used to encrypt the client secret.
    """
    try:
        data = await request.json()
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid JSON in request body")

    if "public_key" not in data:
        raise HTTPException(status_code=400, detail="Missing public_key in request body")

    public_key_b64 = data["public_key"]
    try:
        public_key_bytes = base64.b64decode(public_key_b64)
        # gen_aes_key returns a tuple: (aes_key as hex string, encrypted aes key bytes)
        aes_key, enc_aes_key = gen_aes_key(public_key_bytes)
    except Exception as e:
        raise HTTPException(status_code=400, detail="Invalid public key: " + str(e))

    # Generate client credentials
    client_id = get_random_string(16)
    client_secret = get_random_string(16)
    cle = Client(client_id=client_id, client_secret=client_secret, aes_key=aes_key)
    while cle.check_duplicate():
        client_id = get_random_string(16)
        client_secret = get_random_string(16)
        cle = Client(client_id=client_id, client_secret=client_secret, aes_key=aes_key)
    cle.save()
    logger.info('client id: ' + client_id)
    logger.info('client secret: ' + client_secret)

    # Encrypt client_secret with AES key
    cs_iv, enc_cs = aes_encrypt(client_secret, aes_key)

    return JSONResponse(
        status_code=200,
        content={
            "enc_aes": base64.b64encode(enc_aes_key).decode(),
            "cid": client_id,
            "cs": base64.b64encode(enc_cs).decode(),
            "cs_iv": base64.b64encode(cs_iv).decode()
        }
    )

def aes_decrpt(emsg,aes_key,iv):
    print(aes_key)
    cipha_aes = AES.new(aes_key, AES.MODE_CBC, iv)
    print(aes_key)
    msg = cipha_aes.decrypt(emsg)
    return unpad(msg, AES.block_size)
@app.post("/upload")
async def upload_file(
        request: Request,
        client_id: str = Header(..., alias="Client-Id"),
        client_secret: str = Header(..., alias="Client-Secret"),
        filename: str = Header(..., alias="Filename"),
        iv1: str = Header(...),
        iv2: str = Header(...)
):
    """
    File upload endpoint.

    Expects headers:
      - Client-Id: The client ID.
      - Client-Secret: Base64-encoded AES-encrypted client secret.
      - Filename: Name for the file.
      - iv1: Base64-encoded IV for file data.
      - iv2: Base64-encoded IV for client secret.

    Request body should contain Base64-encoded AES-encrypted file data.
    """
    try:
        # Decode Base64 header values
        encrypted_client_secret = base64.b64decode(client_secret)
        iv1_bytes = base64.b64decode(iv1)
        iv2_bytes = base64.b64decode(iv2)

        # Read and decode the request body (encrypted file data)
        body = await request.body()
        try:
            encrypted_file_data = base64.b64decode(body)
        except Exception:
            raise HTTPException(status_code=400, detail="Invalid Base64 file data in request body")

        logger.info(f"Received upload request from client: {client_id} for file: {filename}")

        # Retrieve the client record from Redis
        client = find_client(client_id=client_id)
        if not client:
            raise HTTPException(status_code=501, detail="Invalid client")

        # Verify the encrypted client secret
        if not client.check_aes_unpad(encrypted_client_secret, iv2_bytes):
            raise HTTPException(status_code=501, detail="Client authentication failed")

        # Decrypt the file data using client's AES key and iv1
        #from util import aes_decrpt
        try:
            decrypted_file = aes_decrpt(encrypted_file_data, client.aes_key.encode(), iv1_bytes)
        except Exception as e:
            raise HTTPException(status_code=500, detail="File decryption failed: " + str(e))

        # Save the decrypted file
        target_dir = "/usr/local/share/data/testing"
        os.makedirs(target_dir, exist_ok=True)
        target_path = os.path.join(target_dir, filename)
        with open(target_path, "ab") as f:
            f.write(decrypted_file)

        logger.info(f"File {filename} stored at {target_path}")
        return JSONResponse(status_code=200, content={"message": f"File {filename} uploaded successfully"})

    except HTTPException as http_exc:
        raise http_exc
    except Exception as e:
        logger.error("Error handling upload: " + str(e))
        raise HTTPException(status_code=500, detail=str(e))


from fastapi.staticfiles import StaticFiles
app.mount("/", StaticFiles(directory="static",html=True), name="static")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
