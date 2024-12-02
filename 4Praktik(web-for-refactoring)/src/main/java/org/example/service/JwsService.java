package org.example.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Service
public class JwsService {

    @Autowired
    private KeyPair keyPair;

    public String createJWS(String message) throws JOSEException {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();

        JWSSigner signer = new RSASSASigner(jwk.toRSAKey());
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.RS256),
                new Payload(message)
        );
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }

    public boolean verifyJWS(String jwsString) throws JOSEException, ParseException {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();

        JWSObject jwsObject = JWSObject.parse(jwsString);
        JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());
        return jwsObject.verify(verifier);
    }
}