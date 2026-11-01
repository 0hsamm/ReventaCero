package co.edu.unbosque.reventacero.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication
        .AuthenticationManager;

import org.springframework.security.authentication
        .AuthenticationProvider;

import org.springframework.security.authentication.dao
        .DaoAuthenticationProvider;

import org.springframework.security.config
        .Customizer;

import org.springframework.security.config.annotation.authentication.configuration
        .AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders
        .HttpSecurity;

import org.springframework.security.config.annotation.web.configuration
        .EnableWebSecurity;

import org.springframework.security.config.http
        .SessionCreationPolicy;

import org.springframework.security.core.userdetails
        .UserDetailsService;

import org.springframework.security.crypto.bcrypt
        .BCryptPasswordEncoder;

import org.springframework.security.crypto.password
        .PasswordEncoder;

import org.springframework.security.web
        .SecurityFilterChain;

import org.springframework.security.web.authentication
        .HttpStatusEntryPoint;

import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors
        .CorsConfiguration;

import org.springframework.web.cors
        .CorsConfigurationSource;

import org.springframework.web.cors
        .UrlBasedCorsConfigurationSource;


/**
 * Configuración general
 * de Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    private final UserDetailsService userDetailsService;

    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            @Value("${app.frontend.allowed-origins}")
            String allowedOriginsRaw) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.allowedOrigins = Arrays.stream(
                        allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Configuración principal
     * de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)

            throws Exception {

        http

                /**
                 * Habilita CORS.
                 */
                .cors(Customizer.withDefaults())

                /**
                 * Deshabilita CSRF.
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Configuración de rutas.
                 */
                .authorizeHttpRequests(auth -> auth

                        /**
                         * Endpoints públicos.
                         */
                        .requestMatchers(
                                "/auth/**")

                        .permitAll()

                        .requestMatchers(
                                "/error")

                        .permitAll()

                        /**
                         * Consulta pública de festivales.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/festivales/**")

                        .permitAll()

                        /**
                         * Gestión de festivales:
                         * organizadores y administradores.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/festivales/**")
                        .hasAnyAuthority("ROLE_ORGANIZADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/festivales/**")
                        .hasAnyAuthority("ROLE_ORGANIZADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/festivales/**")
                        .hasAnyAuthority("ROLE_ORGANIZADOR", "ROLE_ADMINISTRADOR")

                        .requestMatchers("/usuarios/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")

                        .requestMatchers("/admin/**")
                        .hasAuthority("ROLE_ADMINISTRADOR")

                        /**
                         * Todo lo demás requiere
                         * un token válido.
                         */
                        .anyRequest()

                        .authenticated())

                /**
                 * Sin token o con token inválido
                 * responde 401 (sin esto Spring
                 * responde 403).
                 */
                .exceptionHandling(exceptions ->

                        exceptions.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED)))

                /**
                 * JWT = stateless.
                 */
                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                /**
                 * Provider.
                 */
                .authenticationProvider(
                        authenticationProvider())

                /**
                 * Filtro JWT.
                 */
                .addFilterBefore(

                        jwtAuthFilter,

                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provider de autenticación.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(
                        userDetailsService);

        authProvider.setPasswordEncoder(
                passwordEncoder());

        return authProvider;
    }

    /**
     * AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)

            throws Exception {

        return config.getAuthenticationManager();
    }

    /**
     * Encoder BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración CORS.
     */
    @Bean
    public CorsConfigurationSource
            corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                allowedOrigins);

        configuration.setAllowedMethods(

                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"));

        configuration.setAllowedHeaders(
                List.of("*"));

        configuration.setAllowCredentials(
                true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration);

        return source;
    }
}
