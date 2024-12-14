import multiprocessing
import os

def run_server():
    os.system("python server.py")

def run_client():
    os.system("python client_ui.py")

if __name__ == "__main__":
    server_process = multiprocessing.Process(target=run_server)
    server_process.start()
    
    client_process = multiprocessing.Process(target=run_client)
    client_process.start()

    server_process.join()
    client_process.join()
