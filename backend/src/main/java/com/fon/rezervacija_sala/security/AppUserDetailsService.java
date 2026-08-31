package com.fon.rezervacija_sala.security;

import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final KorisnikRepository korisnici;

    public AppUserDetailsService(KorisnikRepository korisnici) {
        this.korisnici = korisnici;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Korisnik k = korisnici.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Korisnik sa email adresom " + email + " nije pronađen."));
        
        boolean enabled = k.getStatus() != StatusNaloga.NEAKTIVAN;

        List<SimpleGrantedAuthority> authorities = k.getUloge().stream()
                .map(u -> new SimpleGrantedAuthority("ROLE_" + u.getNaziv().name()))
                .toList();

        return new User(
                k.getEmail(),
                k.getLozinkaHash(),
                enabled,
                true,
                true,
                true,
                authorities
        );
    }

}