package org.example.service;

import com.nimbusds.jose.jwk.RSAKey;
import org.example.util.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeyService {

    @Autowired
    private KeyUtil keyUtil;

    @Autowired
    private KeyPair keyPair;

    public Map<String, Object> getKeyPair() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        Map<String, Object> keyPairMap = new HashMap<>();
        keyPairMap.put("publicKey", jwk.toPublicJWK().toJSONObject());
        keyPairMap.put("privateKey", jwk.toJSONObject());
        return keyPairMap;
    }
}