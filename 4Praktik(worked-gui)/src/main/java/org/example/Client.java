package org.example;
// src/Client.java

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Client {
    private static KeyPair keyPair;

    public static void main(String[] args) {
        try {
            // Загрузка существующей пары ключей
            keyPair = KeyUtil.loadKeyPair("keypair.ser");
            // Создание JWK из пары ключей
            RSAKey jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .build();
            connectToServer(jwk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void connectToServer(RSAKey jwk) {
        System.out.println("Attempting to connect to the server...");
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Connected to the server.");
            out.println("REQUEST_MESSAGE");
            String message = in.readLine();
            System.out.println("Received from server: " + message);
            if (message != null) {
                try {
                    String jws = createJWS(message, jwk);
                    out.println(jws);
                    System.out.println("Sent to server: " + jws);
                    String response = in.readLine();
                    if ("VALID".equals(response)) {
                        System.out.println("Server response: " + response);
                    } else {
                        System.err.println("Server response: " + response);
                    }
                } catch (JOSEException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("No message received from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createJWS(String message, JWK jwk) throws JOSEException {
        JWSSigner signer = new RSASSASigner(jwk.toRSAKey());
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.RS256),
                new Payload(message)
        );
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }
}