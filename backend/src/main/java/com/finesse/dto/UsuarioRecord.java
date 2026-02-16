package com.finesse.dto;

/**
 * DTO para retornar informações de usuário
 * Não contém a senha
 */
public record UsuarioRecord(
    Long id,
    String nome,
    String email,
    Boolean ativo,
    String perfil,
    Boolean isAdmin
) {
}
