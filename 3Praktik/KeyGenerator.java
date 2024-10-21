// src/KeyGenerator.java
import java.security.*;

public class KeyGenerator {
    public static KeyPair generateDSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }
}