package com.fon.rezervacija_sala.security;

import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwt;
    @Mock
    private AppUserDetailsService uds;
    @Mock
    private KorisnikRepository korisnici;
    @Mock
    private FilterChain chain;

    private JwtAuthFilter filter;

    private static final String EMAIL = "tijana.milosavljevic@fon.bg.ac.rs";
    private static final String TOKEN = "ispravan-token";

    @BeforeEach
    void priprema() {
        filter = new JwtAuthFilter(jwt, uds, korisnici);
        SecurityContextHolder.clearContext();
    }

    private UserDetails napraviKorisnickePodatke() {
        return User.withUsername(EMAIL)
                .password("TestLozinka123!")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void doFilter_optionsZahtev_propustaBezProvere() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/rezervacija");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwt);
    }

    @Test
    void doFilter_bezAuthorizationZaglavlja_propustaBezPostavljanjaAutentifikacije() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rezervacija");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_nevaziciToken_propustaBezPostavljanjaAutentifikacije() throws Exception {
        when(jwt.extractUsername("neispravan")).thenThrow(new RuntimeException("Neispravan token"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rezervacija");
        request.addHeader("Authorization", "Bearer neispravan");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_ispravanTokenAliBlokiranNalog_vraca403IPrekidaLanac() throws Exception {
        UserDetails korisnickiPodaci = napraviKorisnickePodatke();
        Korisnik blokiran = new Korisnik(1L);
        blokiran.setStatus(StatusNaloga.BLOKIRAN);

        when(jwt.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(uds.loadUserByUsername(EMAIL)).thenReturn(korisnickiPodaci);
        when(jwt.isValid(TOKEN, korisnickiPodaci)).thenReturn(true);
        when(korisnici.findByEmail(EMAIL)).thenReturn(Optional.of(blokiran));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rezervacija");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_blokiranNalogAliDozvoljenaPutanja_propustaZahtev() throws Exception {
        UserDetails korisnickiPodaci = napraviKorisnickePodatke();
        Korisnik blokiran = new Korisnik(1L);
        blokiran.setStatus(StatusNaloga.BLOKIRAN);

        when(jwt.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(uds.loadUserByUsername(EMAIL)).thenReturn(korisnickiPodaci);
        when(jwt.isValid(TOKEN, korisnickiPodaci)).thenReturn(true);
        when(korisnici.findByEmail(EMAIL)).thenReturn(Optional.of(blokiran));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_ispravanTokenAktivanNalog_postavljaAutentifikaciju() throws Exception {
        UserDetails korisnickiPodaci = napraviKorisnickePodatke();
        Korisnik aktivan = new Korisnik(1L);
        aktivan.setStatus(StatusNaloga.AKTIVAN);

        when(jwt.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(uds.loadUserByUsername(EMAIL)).thenReturn(korisnickiPodaci);
        when(jwt.isValid(TOKEN, korisnickiPodaci)).thenReturn(true);
        when(korisnici.findByEmail(EMAIL)).thenReturn(Optional.of(aktivan));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rezervacija");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilter_tokenIsValidVracaFalse_nePostavljaAutentifikaciju() throws Exception {
        UserDetails korisnickiPodaci = napraviKorisnickePodatke();

        when(jwt.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(uds.loadUserByUsername(EMAIL)).thenReturn(korisnickiPodaci);
        when(jwt.isValid(TOKEN, korisnickiPodaci)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rezervacija");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }
}