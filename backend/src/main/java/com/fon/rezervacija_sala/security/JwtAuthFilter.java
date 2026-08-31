package com.fon.rezervacija_sala.security;

import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final KorisnikRepository korisnici;

    private static final String DOZVOLJENA_PUTANJA_ZA_BLOKIRANE = "/api/auth/me";

    public JwtAuthFilter(JwtService jwt, AppUserDetailsService uds, KorisnikRepository korisnici) {
        this.jwt = jwt;
        this.uds = uds;
        this.korisnici = korisnici;
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
                    Korisnik k = korisnici.findByEmail(email).orElse(null);
                    boolean jeBlokiran = k != null && k.getStatus() == StatusNaloga.BLOKIRAN;
                    boolean jeDozvoljenaPutanja = req.getRequestURI().startsWith(DOZVOLJENA_PUTANJA_ZA_BLOKIRANE);

                    if (jeBlokiran && !jeDozvoljenaPutanja) {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.setContentType("application/json;charset=UTF-8");
                        res.getWriter().write(
                                "{\"poruka\":\"Vaš nalog je blokiran. Obratite se administratoru.\"}");
                        return;
                    }

                    List<SimpleGrantedAuthority> authorities = ud.getAuthorities().stream()
                            .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                            .toList();

                    UsernamePasswordAuthenticationToken at
                            = new UsernamePasswordAuthenticationToken(ud, null, authorities);
                    at.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(at);
                }
            }
        }

        chain.doFilter(req, res);
    }

}