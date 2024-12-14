import os
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.hazmat.backends import default_backend
from cryptography.x509.oid import NameOID
import datetime


# Получаем путь текущего файла
current_dir = os.path.dirname(os.path.abspath(__file__))


# Этап 1: Проверка доверенности сертификата
def verify_certificate(cert_path, ca_cert_path):
    with open(cert_path, "rb") as f:
        cert = x509.load_pem_x509_certificate(f.read(), default_backend())
    with open(ca_cert_path, "rb") as f:
        ca_cert = x509.load_pem_x509_certificate(f.read(), default_backend())
    try:
        ca_cert.public_key().verify(
            cert.signature,
            cert.tbs_certificate_bytes,
            padding.PKCS1v15(),
            cert.signature_hash_algorithm,
        )
        print("Сертификат доверен!")
    except Exception as e:
        print("Сертификат не доверен:", e)


# Этап 2: Создание самоподписанного X.509 сертификата
def create_self_signed_cert():
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
        backend=default_backend()
    )
    subject = issuer = x509.Name([
        x509.NameAttribute(NameOID.COUNTRY_NAME, u"US"),
        x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, u"California"),
        x509.NameAttribute(NameOID.LOCALITY_NAME, u"San Francisco"),
        x509.NameAttribute(NameOID.ORGANIZATION_NAME, u"My Company"),
        x509.NameAttribute(NameOID.COMMON_NAME, u"mycompany.com"),
    ])
    cert = x509.CertificateBuilder().subject_name(
        subject
    ).issuer_name(
        issuer
    ).public_key(
        private_key.public_key()
    ).serial_number(
        x509.random_serial_number()
    ).not_valid_before(
        datetime.datetime.utcnow()
    ).not_valid_after(
        datetime.datetime.utcnow() + datetime.timedelta(days=365)
    ).add_extension(
        x509.SubjectAlternativeName([x509.DNSName(u"localhost")]),
        critical=False,
    ).sign(private_key, hashes.SHA256(), default_backend())

    # Путь для сохранения сертификата и ключа
    cert_path = os.path.join(current_dir, "my_cert.pem")
    key_path = os.path.join(current_dir, "my_key.pem")

    with open(cert_path, "wb") as f:
        f.write(cert.public_bytes(serialization.Encoding.PEM))
    with open(key_path, "wb") as f:
        f.write(private_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption()
        ))
    print(f"Сертификат создан и сохранён в {cert_path}")


# Этап 3: Подпись сообщения
def sign_message(message, key_path):
    with open(key_path, "rb") as key_file:
        private_key = load_pem_private_key(key_file.read(), password=None, backend=default_backend())
    signature = private_key.sign(
        message,
        padding.PKCS1v15(),
        hashes.SHA256()
    )
    print("Сообщение подписано.")
    return signature


# Этап 4: Проверка подписи
def verify_message(message, signature, cert_path):
    with open(cert_path, "rb") as f:
        cert = x509.load_pem_x509_certificate(f.read(), default_backend())
    public_key = cert.public_key()
    try:
        public_key.verify(
            signature,
            message,
            padding.PKCS1v15(),
            hashes.SHA256()
        )
        print("Подпись подтверждена!")
    except Exception as e:
        print("Ошибка валидации подписи:", e)


# Выполнение всех этапов
if __name__ == "__main__":
    # Этап 1: Проверка доверенности
    # Замените на пути к существующему сертификату и CA
    # verify_certificate("client_cert.pem", "ca_cert.pem")

    # Этап 2: Создание собственного сертификата
    create_self_signed_cert()

    # Этап 3: Подпись сообщения
    message = b"Important message"
    signature = sign_message(message, os.path.join(current_dir, "my_key.pem"))

    # Этап 4: Проверка подписи
    verify_message(message, signature, os.path.join(current_dir, "my_cert.pem"))
