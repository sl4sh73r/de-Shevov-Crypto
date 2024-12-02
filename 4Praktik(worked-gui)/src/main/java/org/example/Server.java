package org.example;
// src/Server.java

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;

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
                    JWSObject jwsObject = JWSObject.parse(clientMessage);
                    
                    // Создание JWK из KeyPair
                    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                    RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
                    
                    JWSVerifier verifier = new RSASSAVerifier(jwk);
                    if (jwsObject.verify(verifier)) {
                        String message = jwsObject.getPayload().toString();
                        out.println("VALID");
                        System.out.println("Sent to client: VALID");
                    } else {
                        out.println("INVALID");
                        System.out.println("Sent to client: INVALID");
                    }
                }
            }
        } catch (IOException | JOSEException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            clientSocket.close();
            System.out.println("Client connection closed.");
        }
    }
}