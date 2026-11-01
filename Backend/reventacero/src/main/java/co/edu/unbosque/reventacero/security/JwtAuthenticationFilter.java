package co.edu.unbosque.reventacero.security;

import java.io.IOException;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context
        .SecurityContextHolder;

import org.springframework.security.core.userdetails
        .UserDetails;

import org.springframework.security.core.userdetails
        .UserDetailsService;

import org.springframework.security.core.userdetails
        .UsernameNotFoundException;

import org.springframework.security.web.authentication
        .WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter
        .OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro encargado de validar
 * el token JWT en cada petición.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)

            throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        String correo = null;

        String jwt = null;

        /**
         * Verifica si el header contiene
         * un token Bearer.
         */
        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            jwt = authorizationHeader.substring(7);

            try {

                correo =
                        jwtUtil.extractUsername(jwt);

            } catch (Exception e) {

                logger.error(
                        "Error extrayendo correo del token",
                        e);
            }
        }

        /**
         * Si existe el correo y no hay
         * autenticación previa.
         */
        if (correo != null
                && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails;

            try {

                userDetails =
                        userDetailsService
                                .loadUserByUsername(correo);

            } catch (UsernameNotFoundException e) {

                /**
                 * El token es de un usuario
                 * que ya no existe: se sigue
                 * sin autenticar (responde 401).
                 */
                filterChain.doFilter(
                        request,
                        response);

                return;
            }

            /**
             * Valida el token.
             */
            if (jwtUtil.validateToken(
                    jwt,
                    userDetails)) {

                UsernamePasswordAuthenticationToken
                        authenticationToken =

                                new UsernamePasswordAuthenticationToken(

                                        userDetails,

                                        null,

                                        userDetails.getAuthorities());

                authenticationToken.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(
                request,
                response);
    }
}