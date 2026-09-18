package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email je obavezan.")
    @Email(message = "Email nije u ispravnom formatu.")
    private String email;

    @NotBlank(message = "Lozinka je obavezna.")
    @Size(min = 6, max = 255, message = "Lozinka mora imati najmanje 6 karaktera.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Lozinka mora sadržati bar jedno slovo i jedan broj.")
    private String lozinka;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

}