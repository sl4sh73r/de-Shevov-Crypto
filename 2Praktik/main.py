from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.hashes import SHA256
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
import os

# Генерация параметров Диффи-Хеллмана
parameters = dh.generate_parameters(generator=2, key_size=2048, backend=default_backend())

# Генерация приватных ключей для A и B
private_key_A = parameters.generate_private_key()
private_key_B = parameters.generate_private_key()

# Получение публичных ключей
public_key_A = private_key_A.public_key()
public_key_B = private_key_B.public_key()

# Обмен публичными ключами и создание общего секрета
shared_secret_A = private_key_A.exchange(public_key_B)
shared_secret_B = private_key_B.exchange(public_key_A)

# Подтверждаем, что общий секрет одинаков для обеих сторон
assert shared_secret_A == shared_secret_B

# Используем HKDF для генерации ключа симметричного шифрования на основе общего секрета
derived_key = HKDF(
    algorithm=SHA256(),
    length=32,
    salt=None,
    info=b'dh key exchange',
    backend=default_backend()
).derive(shared_secret_A)

# Функция шифрования сообщения
def encrypt_message(message: bytes, key: bytes) -> bytes:
    iv = os.urandom(16)
    cipher = Cipher(algorithms.AES(key), modes.CFB(iv), backend=default_backend())
    encryptor = cipher.encryptor()
    ciphertext = encryptor.update(message) + encryptor.finalize()
    return iv + ciphertext

# Функция расшифровки сообщения
def decrypt_message(ciphertext: bytes, key: bytes) -> bytes:
    iv = ciphertext[:16]
    cipher = Cipher(algorithms.AES(key), modes.CFB(iv), backend=default_backend())
    decryptor = cipher.decryptor()
    return decryptor.update(ciphertext[16:]) + decryptor.finalize()

# Пример использования
message = b'Very secret message'
print(f"Original message: {message}")
# A шифрует сообщение
encrypted_message = encrypt_message(message, derived_key)
print(f"Encrypted message: {encrypted_message}")
# B расшифровывает сообщение
decrypted_message = decrypt_message(encrypted_message, derived_key)

print(f"Decrypted message: {decrypted_message}")