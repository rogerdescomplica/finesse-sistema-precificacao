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
import org.springframework.data.domain.Page;

import com.finesse.entity.Atividade;
import com.finesse.repository.AtividadeRepository;

class AtividadeServiceImplTest {

    @Mock
    AtividadeRepository repo;

    @InjectMocks
    AtividadeServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Atividade atividade(Long id, String nome, boolean ativo) {
        Atividade a = new Atividade();
        a.setId(id);
        a.setNome(nome);
        a.setObservacao("desc");
        a.setAtivo(ativo);
        return a;
    }

    @Test
    void list_shouldFilterByTitulo() {
        when(repo.findAll()).thenReturn(List.of(
            atividade(1L, "Planejamento", true),
            atividade(2L, "Execução", false),
            atividade(3L, "Revisão", true)
        ));
        Page<Atividade> page = service.list("Ex", null, 0, 10, "nome,asc");
        assertEquals(1, page.getTotalElements());
        assertEquals("Execução", page.getContent().get(0).getNome());
    }

    @Test
    void create_shouldPersist() {
        Atividade a = atividade(null, "Nova", true);
        when(repo.save(any())).thenAnswer(inv -> {
            Atividade saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        Atividade created = service.create(a);
        assertNotNull(created.getId());
    }

    @Test
    void update_shouldChangeFields() {
        Atividade ex = atividade(5L, "Antiga", true);
        when(repo.findById(5L)).thenReturn(Optional.of(ex));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Atividade input = atividade(null, "Atual", false);
        Optional<Atividade> updated = service.update(5L, input);
        assertTrue(updated.isPresent());
        assertEquals("Atual", updated.get().getNome());
        assertFalse(updated.get().isAtivo());
    }

    @Test
    void delete_shouldReturnFalseWhenNotExists() {
        when(repo.existsById(1L)).thenReturn(false);
        assertFalse(service.delete(1L));
    }

    @Test
    void toggleStatus_shouldSwitch() {
        Atividade ex = atividade(8L, "X", true);
        when(repo.findById(8L)).thenReturn(Optional.of(ex));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Optional<Atividade> toggled = service.toggleStatus(8L, false);
        assertTrue(toggled.isPresent());
        assertFalse(toggled.get().isAtivo());
    }
}
