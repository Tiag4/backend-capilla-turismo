package com.upc.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.demo.dto.common.ErrorResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:4173}")
    private String allowedOrigins;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Bean
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publicos de autenticacion e invitaciones
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/invitations/validate/**").permitAll()
                        
                        // Verificacion de salud (Health Check)
                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/health").permitAll()
                        
                        // Archivos de medios estaticos subidos localmente
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        
                        // Catalogo de alojamientos
                        .requestMatchers("/api/v1/accommodations/my-accommodations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/accommodations/**").permitAll()
                        
                        // Catalogo publico de atractivos turisticos
                        .requestMatchers(HttpMethod.GET, "/api/v1/attractions/**").permitAll()

                        // Eventos culturales publicos (agenda municipal)
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()

                        // Adhesion comercial: postulacion publica
                        .requestMatchers(HttpMethod.POST, "/api/v1/merchants/apply").permitAll()

                        // Alertas meteorologicas y de seguridad publica
                        .requestMatchers(HttpMethod.GET, "/api/v1/alerts/active").permitAll()
                        
                        // Reservas: creacion y consulta publica (lookup)
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/resend-voucher").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/lookup").permitAll()

                        // Resenas: envio publico verificado por codigo y consulta de resenas aprobadas
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/eligibility", "/api/v1/reviews/accommodation/**").permitAll()
                        
                        // Documentacion OpenAPI / Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Cualquier otra peticion requiere autenticacion
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            ErrorResponseDto error = ErrorResponseDto.builder()
                                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                                    .message("No autorizado: se requiere token JWT valido")
                                    .error("Unauthorized")
                                    .timestamp(LocalDateTime.now())
                                    .build();
                            response.getOutputStream().println(objectMapper.writeValueAsString(error));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ErrorResponseDto error = ErrorResponseDto.builder()
                                    .statusCode(HttpStatus.FORBIDDEN.value())
                                    .message("Acceso denegado: no tienes permisos para acceder a este recurso")
                                    .error("Forbidden")
                                    .timestamp(LocalDateTime.now())
                                    .build();
                            response.getOutputStream().println(objectMapper.writeValueAsString(error));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
