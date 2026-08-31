package com.fon.rezervacija_sala.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    @Value("${app.security.max-request-size-bytes:2097152}") 
    private long maksimalnaVelicinaZahteva;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        long duzinaTela = req.getContentLengthLong();

        if (duzinaTela > maksimalnaVelicinaZahteva) {
            res.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            res.setContentType("application/json");
            res.getWriter().write(
                    "{\"poruka\":\"Telo zahteva je preveliko. Maksimalno dozvoljeno: "
                    + maksimalnaVelicinaZahteva + " bajtova.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

}