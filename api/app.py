from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from jose import JWTError, jwt
from passlib.context import CryptContext
from pydantic import BaseModel
from datetime import datetime, timedelta
from typing import Optional, List
import os
from fastapi.responses import FileResponse
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



default_user = UserInDB( username="admin", hashed_password=get_password_hash("PASSWORD") ) 
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

from fastapi.staticfiles import StaticFiles
app.mount("/", StaticFiles(directory="static",html=True), name="static")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
