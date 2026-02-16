package com.finesse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
import com.finesse.exception.ValidationException;
import com.finesse.repository.UsuarioRepository;

class UsuarioServiceImplTest {

    @Mock
    UsuarioRepository repo;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UsuarioServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(encoder.encode(anyString())).thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");
    }

    private Usuario user(Long id, String email, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Nome");
        u.setEmail(email);
        u.setSenha("x");
        u.setAtivo(ativo);
        u.setPerfil(Perfil.VISUALIZADOR);
        return u;
    }

    @Test
    void criarUsuario_deveCriptografarSenha_eValidarEmail() {
        when(repo.existsByEmail("a@b.com")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> { Usuario u = inv.getArgument(0); u.setId(1L); return u; });
        Usuario u = service.criarUsuario(" Nome ", "a@b.com", "123456", Perfil.ADMIN);
        assertNotNull(u.getId());
        assertEquals("ENC(123456)", u.getPassword());
        assertTrue(u.temPerfil(Perfil.ADMIN));
    }

    @Test
    void criarUsuario_deveFalharSeEmailExiste() {
        when(repo.existsByEmail("a@b.com")).thenReturn(true);
        assertThrows(ValidationException.class, () -> service.criarUsuario("n", "a@b.com", "123456", Perfil.ADMIN));
    }

    @Test
    void alterarSenha_deveCriptografar() {
        when(repo.findById(2L)).thenReturn(Optional.of(user(2L, "x@y.com", true)));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.alterarSenha(2L, "abcdef");
        verify(repo).save(any());
    }
    
    @Test
    void listarAtivos_eContar() {
        when(repo.findAll()).thenReturn(List.of(user(1L, "a@b.com", true), user(2L, "c@d.com", false)));
        when(repo.count()).thenReturn(2L);
        List<Usuario> ativos = service.listarAtivos();
        assertEquals(1, ativos.size());
        assertEquals(2, service.contarTodos());
        assertEquals(1, service.contarAtivos());
    }

    @Test
    void emailExiste_deveDelegarParaRepo() {
        when(repo.existsByEmail("x@y.com")).thenReturn(true);
        assertTrue(service.emailExiste("x@y.com"));
    }
}
