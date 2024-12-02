package org.example.config;

import org.example.util.KeyUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;

@Configuration
public class KeyConfig {

    @Bean
    public KeyPair keyPair(KeyUtil keyUtil) {
        try {
            return keyUtil.loadKeyPair("keypair.ser");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load key pair", e);
        }
    }
}