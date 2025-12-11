package com.finesse.dto;

import com.finesse.entity.Perfil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequestRecord(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    String senha,
    
    @NotNull(message = "Perfil é obrigatório")
    Perfil perfil
) {
    @Override
    public String toString() {
        return "CriarUsuarioRequest{nome='" + nome + "', email='" + email + 
               "', senha='[PROTECTED]', perfil=" + perfil + "}";
    }
}
