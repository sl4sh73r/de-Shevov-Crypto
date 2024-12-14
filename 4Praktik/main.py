import jwt
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
import random
import json

# Функция для генерации пары RSA ключей
def generate_rsa_keys():
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
    )
    
    public_key = private_key.public_key()
    
    # Приватный ключ в формате PEM
    private_key_pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.NoEncryption()
    )

    # Публичный ключ в формате PEM
    public_key_pem = public_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
    )
    
    return private_key_pem, public_key_pem

# Функция для генерации подписи с использованием JWS
def generate_signature(message, private_key):
    # Преобразуем приватный ключ из PEM в объект
    private_key_obj = serialization.load_pem_private_key(private_key, password=None)
    
    # Генерация JWS подписи с использованием библиотеки jwt
    signed_message = jwt.encode({'message': message}, private_key_obj, algorithm='RS256')
    
    return signed_message

# Функция для проверки подписи с использованием JWS
def verify_signature(message, signed_message, public_key):
    # Преобразуем публичный ключ из PEM в объект
    public_key_obj = serialization.load_pem_public_key(public_key)
    
    try:
        # Декодирование и проверка подписи
        decoded_message = jwt.decode(signed_message, public_key_obj, algorithms=["RS256"])
        print(f"Декодированное сообщение: {decoded_message}")
        return decoded_message['message'] == message
    except jwt.InvalidTokenError as e:
        print(f"Ошибка при декодировании подписи: {e}")
        return False

# Клиент
class Client:
    def __init__(self, private_key, public_key):
        self.private_key = private_key
        self.public_key = public_key
        self.increment = 1
    
    def sign_message(self, message):
        # Добавление инкремента к сообщению
        message_with_increment = message + str(self.increment)
        
        # Подписание сообщения
        signed_message = generate_signature(message_with_increment, self.private_key)
        print(f"Клиент подписал сообщение: {message_with_increment} с инкрементом: {self.increment}")
        print(f"Подпись клиента (JWS): {signed_message}")
        return signed_message, self.increment

# Сервер
class Server:
    def __init__(self, private_key, public_key):
        self.private_key = private_key
        self.public_key = public_key
        self.increment = 1
    
    def generate_new_message(self):
        new_message = str(random.randint(1000, 9999))  # Генерация случайного числа
        print(f"Сервер сгенерировал новое сообщение: {new_message}")
        return new_message
    
    def verify_message(self, signed_message, message, increment):
        # Формируем сообщение с инкрементом, которое должно быть подписано
        message_with_increment = message + str(increment)
        print(f"Проверка подписи для сообщения с инкрементом: {message_with_increment}")
        
        is_valid = verify_signature(message_with_increment, signed_message, self.public_key)
        print(f"Подпись {'валидна' if is_valid else 'невалидна'}")
        return is_valid
    
    def check_increment(self, increment):
        if increment >= self.increment:
            self.increment = increment + 1
            print(f"Инкремент обновлен до: {self.increment}")
            return True
        print(f"Инкремент не обновлен. Текущий инкремент: {self.increment}, полученный инкремент: {increment}")
        return False

# Пример использования
private_key_pem, public_key_pem = generate_rsa_keys()

server = Server(private_key_pem, public_key_pem)
client = Client(private_key_pem, public_key_pem)

# Генерация сообщения
message = server.generate_new_message()

# Клиент подписывает сообщение
signed_message, increment = client.sign_message(message)

# Отправка сообщения и проверка подписи на сервере
if server.verify_message(signed_message, message, increment) and server.check_increment(increment):
    print("Сообщение проверено успешно")
else:
    print("Ошибка проверки подписи или инкремента")
