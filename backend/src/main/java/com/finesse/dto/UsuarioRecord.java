package com.finesse.dto;

import java.util.List;

/**
 * DTO para retornar informações de usuário
 * Não contém a senha
 */
public record UsuarioRecord(
    Long id,
    String nome,
    String email,
    Boolean ativo,
    List<String> perfis,
    Boolean isAdmin
) {
}
