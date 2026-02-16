package com.finesse.service;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
import com.finesse.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;


public class UsuarioServiceTests {
    @Test
    void criarUsuario_validaDuplicidadeEmail() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        UsuarioServiceImpl service = new UsuarioServiceImpl(repo, encoder);

        Mockito.when(repo.existsByEmail("teste@x.com")).thenReturn(true);
        Assertions.assertThrows(RuntimeException.class, () -> service.criarUsuario("Teste", "teste@x.com", "senha123", Perfil.ADMIN));
    }

    @Test
    void criarUsuario_rejeitaSenhaFraca_eAceitaForte() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        UsuarioServiceImpl service = new UsuarioServiceImpl(repo, encoder);

        Mockito.when(repo.existsByEmail("ok@x.com")).thenReturn(false);
        Mockito.when(encoder.encode(Mockito.anyString())).thenAnswer(inv -> "hash");
        Mockito.when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        // senha fraca
        Assertions.assertThrows(RuntimeException.class, () -> service.criarUsuario("Ok", "ok@x.com", "abc123", Perfil.ADMIN));
        // senha forte: 8+ com maiúscula/minúscula/número/especial
        Usuario u = service.criarUsuario("Ok", "ok@x.com", "Abcdef1!", Perfil.ADMIN);
        Assertions.assertNotNull(u);
    }

}
