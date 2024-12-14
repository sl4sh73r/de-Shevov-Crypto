import tkinter as tk
from tkinter import ttk
import requests

SERVER_URL = "http://127.0.0.1:5000"

def create_user():
    public_key = entry_public_key.get()
    initial_balance = entry_balance.get()
    
    if not public_key or not initial_balance:
        label_result.config(text="Заполните все поля")
        return

    try:
        initial_balance = int(initial_balance)
    except ValueError:
        label_result.config(text="Баланс должен быть числом")
        return

    response = requests.post(f"{SERVER_URL}/create_user", json={
        'public_key': public_key,
        'initial_balance': initial_balance
    })
    if response.ok:
        label_result.config(text="Пользователь создан")
        refresh_user_list()
    else:
        label_result.config(text=f"Ошибка: {response.status_code}")

def transfer():
    sender_key = entry_sender.get()
    recipient_key = entry_recipient.get()
    amount = entry_amount.get()
    
    if not sender_key or not recipient_key or not amount:
        label_result.config(text="Заполните все поля")
        return

    try:
        amount = int(amount)
    except ValueError:
        label_result.config(text="Сумма должна быть числом")
        return

    response = requests.post(f"{SERVER_URL}/transfer", json={
        'sender_key': sender_key,
        'recipient_key': recipient_key,
        'amount': amount,
        'signature': "dummy_signature"
    })
    if response.ok:
        label_result.config(text="Перевод выполнен")
        refresh_user_list()
    elif response.status_code == 400 and "Insufficient funds" in response.text:
        label_result.config(text="Недостаточно средств")
    else:
        label_result.config(text=f"Ошибка: {response.status_code}")

def refresh_user_list():
    try:
        # Очистка текущей таблицы
        for row in tree.get_children():
            tree.delete(row)

        # Получение данных с сервера
        response = requests.get(f"{SERVER_URL}/users")
        if response.ok:
            users = response.json()
            for public_key, balance in users.items():
                tree.insert("", "end", values=(public_key, balance))
        else:
            label_result.config(text="Не удалось обновить список пользователей")
    except Exception as e:
        label_result.config(text=f"Ошибка обновления: {e}")

def schedule_refresh():
    refresh_user_list()
    root.after(5000, schedule_refresh)  # Обновление каждые 5 секунд

root = tk.Tk()
root.title("Financial Transfer System")

frame_top = tk.Frame(root)
frame_top.pack()

tk.Label(frame_top, text="Public Key").grid(row=0, column=0)
entry_public_key = tk.Entry(frame_top)
entry_public_key.grid(row=0, column=1)

tk.Label(frame_top, text="Initial Balance").grid(row=1, column=0)
entry_balance = tk.Entry(frame_top)
entry_balance.grid(row=1, column=1)

btn_create = tk.Button(frame_top, text="Create User", command=create_user)
btn_create.grid(row=2, column=0, columnspan=2)

tk.Label(frame_top, text="Sender Key").grid(row=3, column=0)
entry_sender = tk.Entry(frame_top)
entry_sender.grid(row=3, column=1)

tk.Label(frame_top, text="Recipient Key").grid(row=4, column=0)
entry_recipient = tk.Entry(frame_top)
entry_recipient.grid(row=4, column=1)

tk.Label(frame_top, text="Amount").grid(row=5, column=0)
entry_amount = tk.Entry(frame_top)
entry_amount.grid(row=5, column=1)

btn_transfer = tk.Button(frame_top, text="Transfer", command=transfer)
btn_transfer.grid(row=6, column=0, columnspan=2)

label_result = tk.Label(root, text="")
label_result.pack()

frame_table = tk.Frame(root)
frame_table.pack()

tree = ttk.Treeview(frame_table, columns=("Public Key", "Balance"), show="headings")
tree.heading("Public Key", text="Public Key")
tree.heading("Balance", text="Balance")
tree.pack()

# Начальное обновление списка пользователей
schedule_refresh()

root.mainloop()
