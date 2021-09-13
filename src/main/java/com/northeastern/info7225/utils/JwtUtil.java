package com.northeastern.info7225.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.util.Date;

public class JwtUtil {
    private static RSAKey rsaJWK;

    static {
        try {
            rsaJWK = new RSAKeyGenerator(2048).keyID("abcdefghijklmnopqrstuvwxyz123456789").generate();
        } catch (JOSEException e) {
            e.printStackTrace();
        }

    }

    public static String generateToken() throws JOSEException {
        JWSSigner signer = new RSASSASigner(rsaJWK);
        int expireTime = 300; // seconds
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + expireTime * 1000)) // milliseconds
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claimsSet);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static boolean authorized(HttpHeaders requestHeaders) throws JOSEException, ParseException {
        if (requestHeaders.getFirst("Authorization") == null) {
            return false;
        }
        String token = requestHeaders.getFirst("Authorization").substring(7);
        SignedJWT signedJWT = SignedJWT.parse(token);
        RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();
        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
        if (!signedJWT.verify(verifier)) {
            return false;
        }
        JWTClaimsSet claimset = signedJWT.getJWTClaimsSet();
        Date exp = claimset.getExpirationTime();
        return new Date().before(exp);
    }
}
