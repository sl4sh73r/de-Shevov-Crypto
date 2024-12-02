package org.example;

// src/KeyUtil.java
import java.io.*;
import java.security.*;

public class KeyUtil {
    public static void saveKeyPair(KeyPair keyPair, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(keyPair);
        }
    }

    public static KeyPair loadKeyPair(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (KeyPair) ois.readObject();
        }
    }
}
