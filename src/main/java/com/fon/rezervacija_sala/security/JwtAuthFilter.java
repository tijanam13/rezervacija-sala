package com.fon.rezervacija_sala.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final AppUserDetailsService uds;

    public JwtAuthFilter(JwtService jwt, AppUserDetailsService uds) {
        this.jwt = jwt;
        this.uds = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = null;

            try {
                email = jwt.extractUsername(token);
            } catch (Exception e) {
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails ud;
                try {
                    ud = uds.loadUserByUsername(email);
                } catch (Exception e) {
                    ud = null;
                }

                if (ud != null && jwt.isValid(token, ud) && ud.isEnabled()) {
                    List<SimpleGrantedAuthority> tokenAuthorities = jwt.extractRoles(token).stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList();

                    List<SimpleGrantedAuthority> userAuthorities = ud.getAuthorities().stream()
                            .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                            .toList();

                    List<SimpleGrantedAuthority> combinedAuthorities = new ArrayList<>();
                    combinedAuthorities.addAll(tokenAuthorities);
                    combinedAuthorities.addAll(userAuthorities);

                    UsernamePasswordAuthenticationToken at
                            = new UsernamePasswordAuthenticationToken(ud, null, combinedAuthorities);
                    at.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(at);
                }
            }
        }

        chain.doFilter(req, res);
    }

}