package com.los_maestros.usuarios.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expiration-ms}") private long expirationMs;
    @Value("${app.jwt.issuer}") private String issuer;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .issuer(issuer)
            .subject(usuario.getCorreo())
            .claim("rol", usuario.getRol())
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expirationMs))
            .signWith(clave())
            .compact();
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
