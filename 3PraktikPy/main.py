import time
import random
import socket
import threading
from flask import Flask, request, jsonify

# Генерация цифровой подписи (простая строковая конкатенация)
def generate_signature(message, private_key):
    return f"{message}-{private_key}"

# Клиент
class Client:
    def __init__(self, private_key, public_key, server_public_key):
        self.private_key = private_key
        self.public_key = public_key
        self.server_public_key = server_public_key
        self.increment = 1  # Начинаем с 1, чтобы избежать проблем с нулевым значением
    
    def sign_message(self, message):
        signed_message = generate_signature(message + str(self.increment), self.private_key)
        print(f"Клиент подписал сообщение: {message} с инкрементом: {self.increment}")
        print(f"Подпись клиента: {signed_message}")
        return signed_message, self.increment

# Сервер
class Server:
    def __init__(self, private_key, public_key):
        self.private_key = private_key
        self.public_key = public_key
        self.increment = 1  # Начинаем с 1, чтобы избежать проблем с нулевым значением
    
    def generate_new_message(self):
        new_message = str(random.randint(1000, 9999))
        print(f"Сервер сгенерировал новое сообщение: {new_message}")
        return new_message
    
    def verify_message(self, message, signed_message, client_private_key, increment):
        expected_signature = generate_signature(message + str(increment), client_private_key)
        print(f"Сервер проверяет сообщение: {message} с инкрементом: {increment}")
        print(f"Ожидаемая подпись: {expected_signature}")
        print(f"Полученная подпись: {signed_message}")
        is_valid = expected_signature == signed_message
        print(f"Подпись {'валидна' if is_valid else 'невалидна'}")
        return is_valid
    
    def check_increment(self, increment):
        # Инкремент обновляется только если он больше текущего
        if increment >= self.increment:
            self.increment = increment + 1  # Увеличиваем инкремент для следующего использования
            print(f"Инкремент обновлен до: {self.increment}")
            return True
        print(f"Инкремент не обновлен. Текущий инкремент: {self.increment}, полученный инкремент: {increment}")
        return False

# Злоумышленник (для симуляции атаки)
class Attacker:
    def __init__(self):
        self.intercepted_message = None
        self.intercepted_signature = None
        self.intercepted_increment = None
    
    def intercept(self, message, signed_message, increment=None):
        self.intercepted_message = message
        self.intercepted_signature = signed_message
        self.intercepted_increment = increment
        print(f"Злоумышленник перехватил сообщение: {message}, подпись: {signed_message}, инкремент: {increment}")
    
    def replay_attack(self, server, client_private_key):
        print("Злоумышленник пытается повторить атаку...")
        if self.intercepted_increment is not None:
            return server.verify_message(self.intercepted_message, self.intercepted_signature, client_private_key, self.intercepted_increment)
        return False

# Пример использования
server_private_key = "server_key"
server_public_key = "server_public_key"
client_private_key = "client_private_key"
client_public_key = "client_public_key"

server = Server(server_private_key, server_public_key)
client = Client(client_private_key, client_public_key, server_public_key)
attacker = Attacker()

# Генерация сообщения
message = server.generate_new_message()

# Клиент подписывает сообщение
signed_message, increment = client.sign_message(message)

# Отправка сообщения и проверка подписи на сервере
if server.verify_message(message, signed_message, client_private_key, increment) and server.check_increment(increment):
    print("Сообщение проверено успешно")
else:
    print("Ошибка проверки подписи или инкремента")

# Симуляция атаки
attacker.intercept(message, signed_message, increment)
if attacker.replay_attack(server, client_private_key):
    print("Атака провалилась")
else:
    print("Атака успешна")