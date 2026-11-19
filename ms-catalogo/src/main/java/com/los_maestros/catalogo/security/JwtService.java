package com.los_maestros.catalogo.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.issuer}") private String issuer;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims validar(String token) {
        return Jwts.parser()
            .verifyWith(clave())
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
