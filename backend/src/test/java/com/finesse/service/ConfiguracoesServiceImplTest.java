package com.finesse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.finesse.entity.Configuracoes;
import com.finesse.repository.ConfiguracoesRepository;

class ConfiguracoesServiceImplTest {

    @Mock
    ConfiguracoesRepository repo;

    @InjectMocks
    ConfiguracoesServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Configuracoes cfg(Long id, boolean ativo) {
        Configuracoes c = new Configuracoes();
        c.setId(id);
        c.setPretensaoSalarialMensal(new BigDecimal("1000"));
        c.setHorasSemanais(new BigDecimal("40"));
        c.setSemanasMediaMes(new BigDecimal("4.33"));
        c.setCustoFixoPct(new BigDecimal("0.10"));
        c.setMargemLucroPadraoPct(new BigDecimal("0.25"));
        c.setAtivo(ativo);
        return c;
    }

    @Test
    void list_shouldUseRepository() {
        when(repo.findAll(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(java.util.List.of(cfg(1L, true)), PageRequest.of(0,10), 1));
        Page<Configuracoes> p = service.list(null, 0, 10, "id");
        assertEquals(1, p.getTotalElements());
    }

    @Test
    void create_shouldSanitizePositiveValues() {
        Configuracoes c = cfg(null, true);
        c.setPretensaoSalarialMensal(new BigDecimal("-1500"));
        when(repo.save(any())).thenAnswer(inv -> { Configuracoes s = inv.getArgument(0); s.setId(3L); return s; });
        Configuracoes saved = service.create(c);
        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("1500.00"), saved.getPretensaoSalarialMensal());
    }

    @Test
    void update_shouldPersistChanges() {
        Configuracoes ex = cfg(9L, true);
        when(repo.findById(9L)).thenReturn(Optional.of(ex));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Configuracoes in = cfg(null, false);
        Optional<Configuracoes> updated = service.update(9L, in);
        assertTrue(updated.isPresent());
        assertFalse(updated.get().isAtivo());
    }

    @Test
    void delete_shouldReturnFalseIfMissing() {
        when(repo.existsById(5L)).thenReturn(false);
        assertFalse(service.delete(5L));
    }

    @Test
    void toggleStatus_shouldChangeFlag() {
        Configuracoes ex = cfg(4L, true);
        when(repo.findById(4L)).thenReturn(Optional.of(ex));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Optional<Configuracoes> res = service.toggleStatus(4L, false);
        assertTrue(res.isPresent());
        assertFalse(res.get().isAtivo());
    }
}
