package com.finesse.entity;

/**
 * Enumeração dos perfis/roles de acesso do sistema
 */
public enum Perfil {
    ADMIN("ROLE_ADMIN", "Administrador"),
    VISUALIZADOR("ROLE_VISUALIZADOR", "Visualizador");

    private final String role;
    private final String descricao;

    Perfil(String role, String descricao) {
        this.role = role;
        this.descricao = descricao;
    }

    public String getRole() {
        return role;
    }

    public String getDescricao() {
        return descricao;
    }
}