package com.los_maestros.usuarios.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private final SecretKey clave;
	private final long expiracionMs;

	public JwtService(@Value("${app.jwt.secret}") String secreto,
					  @Value("${app.jwt.expiration-ms}") long expiracionMs) {
		this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
		this.expiracionMs = expiracionMs;
	}

	public String generar(Usuario usuario) {
		Date ahora = new Date();
		return Jwts.builder()
				.subject(usuario.getCorreo())
				.claim("id", usuario.getId())
				.claim("nombre", usuario.getNombre())
				.claim("rol", usuario.getRol())
				.issuedAt(ahora)
				.expiration(new Date(ahora.getTime() + expiracionMs))
				.signWith(clave)
				.compact();
	}

	public Claims leer(String token) {
		return Jwts.parser().verifyWith(clave).build()
				.parseSignedClaims(token).getPayload();
	}
}