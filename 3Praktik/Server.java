// src/Server.java
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class Server {
    private static int increment = 0;
    private static KeyPair keyPair;

    public static void main(String[] args) {
        try {
            keyPair = KeyUtil.loadKeyPair("keypair.ser");
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server started on port 8080");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);

                if ("REQUEST_MESSAGE".equals(clientMessage)) {
                    String message = "Message" + increment++;
                    out.println(message);
                    System.out.println("Sent to client: " + message);
                } else {
                    String[] parts = clientMessage.split(":");
                    String message = parts[0];
                    String signature = parts[1];

                    if (verifySignature(message, signature, keyPair.getPublic())) {
                        out.println("VALID");
                        System.out.println("Sent to client: VALID");
                    } else {
                        out.println("INVALID");
                        System.out.println("Sent to client: INVALID");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            clientSocket.close();
            System.out.println("Client connection closed.");
        }
    }

    private static boolean verifySignature(String message, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA1withDSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        boolean isValid = sig.verify(Base64.getDecoder().decode(signature));
        System.out.println("Verifying signature for message: " + message);
        System.out.println("Signature: " + signature);
        System.out.println("Is valid: " + isValid);
        return isValid;
    }
}