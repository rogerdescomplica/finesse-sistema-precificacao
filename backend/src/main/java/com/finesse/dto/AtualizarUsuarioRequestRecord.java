package com.finesse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequestRecord(
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,
    
    @Email(message = "Email inválido")
    String email,
    
    String perfil  
) {
}
