package com.finesse.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestRecord(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Senha é obrigatório")
    String senha
) {
    @Override
    public String toString() {
        return "LoginRequest{email='" + email + "', password='[PROTECTED]'}";
    }
}
