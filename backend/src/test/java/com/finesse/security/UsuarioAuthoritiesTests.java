package com.finesse.security;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class UsuarioAuthoritiesTests {

    @Test
    void adminUser_hasRoleAdmin() {
        Usuario u = new Usuario("Admin", "admin@sistema.com", "hash");
        u.setPerfil(Perfil.ADMIN);
        List<String> roles = u.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        Assertions.assertTrue(roles.contains("ROLE_ADMIN"));
        Assertions.assertFalse(roles.contains("ROLE_VISUALIZADOR"));
        Assertions.assertTrue(u.isEnabled());
    }

    @Test
    void viewerUser_hasRoleVisualizador() {
        Usuario u = new Usuario("Viewer", "viewer@sistema.com", "hash");
        u.setPerfil(Perfil.VISUALIZADOR);
        List<String> roles = u.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        Assertions.assertTrue(roles.contains("ROLE_VISUALIZADOR"));
        Assertions.assertFalse(roles.contains("ROLE_ADMIN"));
        Assertions.assertTrue(u.isEnabled());
    }

    @Test
    void inativoUser_isDisabled() {
        Usuario u = new Usuario("X", "x@sistema.com", "hash");
        u.setPerfil(Perfil.VISUALIZADOR);
        u.setAtivo(false);
        Assertions.assertFalse(u.isEnabled());
    }
}
