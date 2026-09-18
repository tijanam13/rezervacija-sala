package com.fon.rezervacija_sala.config;

import com.fon.rezervacija_sala.security.AppUserDetailsService;
import com.fon.rezervacija_sala.security.JwtAuthFilter;
import com.fon.rezervacija_sala.security.RequestSizeLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final RequestSizeLimitFilter requestSizeLimitFilter;
    private final AppUserDetailsService uds;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthFilter jwtFilter, RequestSizeLimitFilter requestSizeLimitFilter,
            AppUserDetailsService uds) {
        this.jwtFilter = jwtFilter;
        this.requestSizeLimitFilter = requestSizeLimitFilter;
        this.uds = uds;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.accessDeniedHandler((req, res, ex) -> {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                            "{\"poruka\":\"Nemate dozvolu za ovu radnju.\"}");
                }) 
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"poruka\":\"Niste prijavljeni.\"}");
                })  
                )
                .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(contentType -> {
                })
                .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000))
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
  
                .requestMatchers(HttpMethod.GET, "/api/katedra/**", "/api/zvanje/**", "/api/sluzba/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/rezervacija").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/rezervacija/zauzetost").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/rezervacija").hasAnyAuthority("ROLE_KOORDINATOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/rezervacija/*/status").hasAnyAuthority("ROLE_KOORDINATOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/rezervacija/*/odbij-sa-razlogom").hasAnyAuthority("ROLE_KOORDINATOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/rezervacija/stavka/*/status").hasAnyAuthority("ROLE_KOORDINATOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/rezervacija/stavka/*/odbij-sa-razlogom").hasAnyAuthority("ROLE_KOORDINATOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/rezervacija/oznaci-istekle").hasAuthority("ROLE_ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/sala/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/sala/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/sala/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/sala/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/katedra/**", "/api/zvanje/**", "/api/sluzba/**", "/api/tip-sale/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/katedra/**", "/api/zvanje/**", "/api/sluzba/**", "/api/tip-sale/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/katedra/**", "/api/zvanje/**", "/api/sluzba/**", "/api/tip-sale/**").hasAuthority("ROLE_ADMIN")

                .requestMatchers("/api/korisnik/**").hasAuthority("ROLE_ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/predavac", "/api/sluzbenik").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/predavac/**", "/api/sluzbenik/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/predavac/**", "/api/sluzbenik/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestSizeLimitFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(frontendUrl));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

}