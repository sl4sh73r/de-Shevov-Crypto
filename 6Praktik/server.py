from flask import Flask, request, jsonify

app = Flask(__name__)

# Словарь для хранения данных пользователей
accounts = {}

@app.route('/create_user', methods=['POST'])
def create_user():
    data = request.json
    public_key = data.get('public_key')
    initial_balance = data.get('initial_balance')
    
    if public_key in accounts:
        return "User already exists", 400

    accounts[public_key] = initial_balance
    return "User created", 200

@app.route('/transfer', methods=['POST'])
def transfer():
    data = request.json
    sender_key = data.get('sender_key')
    recipient_key = data.get('recipient_key')
    amount = data.get('amount')
    
    if sender_key not in accounts:
        return "Sender not found", 404
    if recipient_key not in accounts:
        return "Recipient not found", 404
    if accounts[sender_key] < amount:
        return "Insufficient funds", 400

    accounts[sender_key] -= amount
    accounts[recipient_key] += amount
    return "Transfer completed", 200

# Добавляем маршрут для получения списка пользователей
@app.route('/users', methods=['GET'])
def get_users():
    return jsonify(accounts)

if __name__ == '__main__':
    app.run(debug=True)
