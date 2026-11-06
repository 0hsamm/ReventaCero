package co.edu.unbosque.reventacero.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails
        .UserDetails;

import org.springframework.stereotype.Component;

import co.edu.unbosque.reventacero.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Clase encargada del manejo
 * de tokens JWT.
 */
@Component
public class JwtUtil {

    /**
     * Tiempo de validez:
     * 24 horas.
     */
    private static final long JWT_TOKEN_VALIDITY =
            24 * 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Genera la llave de firma.
     */
    private Key getSigningKey() {

        byte[] keyBytes =
                secret.getBytes();

        return Keys.hmacShaKeyFor(
                keyBytes);
    }

    /**
     * Extrae el correo del token.
     */
    public String extractUsername(
            String token) {

        return extractClaim(
                token,
                Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración.
     */
    public Date extractExpiration(
            String token) {

        return extractClaim(
                token,
                Claims::getExpiration);
    }

    /**
     * Extrae cualquier claim.
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(
                claims);
    }

    /**
     * Obtiene todos los claims.
     */
    private Claims extractAllClaims(
            String token) {

        return Jwts.parserBuilder()

                .setSigningKey(
                        getSigningKey())

                .build()

                .parseClaimsJws(token)

                .getBody();
    }

    /**
     * Verifica si el token expiró.
     */
    private Boolean isTokenExpired(
            String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    /**
     * Genera el token JWT.
     */
    public String generateToken(
            UserDetails userDetails) {

        Map<String, Object> claims =
                new HashMap<>();

        /**
         * Si quieres guardar más
         * información del usuario
         * en el token.
         */
        if (userDetails instanceof Usuario) {

            Usuario usuario =
                    (Usuario) userDetails;

            claims.put(
                    "documento",
                    usuario.getDocumento());

            claims.put(
                    "nombreUsuario",
                    usuario.getNombreUsuario());

            claims.put(
                    "correo",
                    usuario.getCorreo());

            claims.put(
                    "tipoUsuario",
                    usuario.getTipoUsuario() != null
                            ? usuario.getTipoUsuario().name()
                            : null);
        }

        return createToken(
                claims,
                userDetails.getUsername());
    }

    /**
     * Crea el JWT.
     */
    private String createToken(
            Map<String, Object> claims,
            String subject) {

        return Jwts.builder()

                .setClaims(claims)

                .setSubject(subject)

                .setIssuedAt(
                        new Date(
                                System.currentTimeMillis()))

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + JWT_TOKEN_VALIDITY))

                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256)

                .compact();
    }

    /**
     * Valida el token.
     */
    public Boolean validateToken(
            String token,
            UserDetails userDetails) {

        final Claims claims =
                extractAllClaims(token);

        return claims.get(CLAIM_PROPOSITO) == null

                && claims.getSubject() != null

                && claims.getSubject().equals(
                        userDetails.getUsername())

                && claims.getExpiration() != null

                && !claims.getExpiration()
                        .before(new Date());
    }

    static final String CLAIM_PROPOSITO = "proposito";

    static final String PROPOSITO_VERIFICAR_CORREO = "VERIFICAR_CORREO";

    public String generarTokenVerificacion(
            Usuario usuario,
            long horasValidez) {

        long ahora = System.currentTimeMillis();

        return Jwts.builder()
                .claim(CLAIM_PROPOSITO, PROPOSITO_VERIFICAR_CORREO)
                .claim("correo", usuario.getCorreo())
                .setSubject(String.valueOf(usuario.getDocumento()))
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + horasValidez * 60 * 60 * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public java.util.Optional<VerificacionCorreo> leerTokenVerificacion(
            String token) {

        try {
            Claims claims = extractAllClaims(token);

            if (!PROPOSITO_VERIFICAR_CORREO.equals(claims.get(CLAIM_PROPOSITO))) {
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(new VerificacionCorreo(
                    Long.parseLong(claims.getSubject()),
                    claims.get("correo", String.class)));

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    public record VerificacionCorreo(long documento, String correo) {
    }
}