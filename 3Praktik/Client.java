// src/Client.java
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class Client {
    private static KeyPair keyPair;

    public static void main(String[] args) {
        try {
            keyPair = KeyUtil.loadKeyPair("keypair.ser");
            connectToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void connectToServer() {
        System.out.println("Attempting to connect to the server...");
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to the server.");
            out.println("REQUEST_MESSAGE");
            String message = in.readLine();
            System.out.println("Received from server: " + message);

            if (message != null) {
                String signature = signMessage(message, keyPair.getPrivate());
                out.println(message + ":" + signature);
                System.out.println("Sent to server: " + message + ":" + signature);

                String response = in.readLine();
                if (response != null) {
                    System.out.println("Server response: " + response);
                } else {
                    System.err.println("No response received from server.");
                }
            } else {
                System.err.println("No message received from server.");
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA1withDSA");
        sig.initSign(privateKey);
        sig.update(message.getBytes());
        String signedMessage = Base64.getEncoder().encodeToString(sig.sign());
        System.out.println("Signing message: " + message);
        System.out.println("Signed message: " + signedMessage);
        return signedMessage;
    }
}