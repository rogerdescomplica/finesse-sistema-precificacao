package com.finesse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import com.finesse.entity.Material;
import com.finesse.entity.UnidadeMedida;
import com.finesse.repository.MaterialRepository;

class MaterialServiceImplTest {

    @Mock
    MaterialRepository repo;

    @InjectMocks
    MaterialServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Material material(Long id, String produto, boolean ativo) {
        Material m = new Material();
        m.setId(id);
        m.setProduto(produto);
        m.setUnidadeMedida(UnidadeMedida.UN);
        m.setVolumeEmbalagem(new BigDecimal("10.00"));
        m.setPrecoEmbalagem(new BigDecimal("50.00"));
        m.setAtivo(ativo);
        return m;
    }

    @Test
    void list_shouldFilterByProdutoAndSortAsc() {
        when(repo.findAll()).thenReturn(List.of(
            material(2L, "Sabonete", true),
            material(1L, "Álcool", true),
            material(3L, "Shampoo", false)
        ));

        Page<Material> page = service.list("a", null, 0, 10, "produto,asc");
        assertEquals(2, page.getTotalElements());
        assertEquals("Sabonete", page.getContent().get(0).getProduto());
    }

    @Test
    void create_shouldSanitizeAndPersist() {
        Material m = material(null, "  Produto  ", true);
        m.setVolumeEmbalagem(new BigDecimal("-5"));
        m.setPrecoEmbalagem(new BigDecimal("-12.345"));

        when(repo.save(any())).thenAnswer(inv -> {
            Material saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Material created = service.create(m);
        assertNotNull(created.getId());

        ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);
        verify(repo).save(captor.capture());
        Material persisted = captor.getValue();
        assertEquals("Produto", persisted.getProduto());
        assertEquals(new BigDecimal("5.00"), persisted.getVolumeEmbalagem());
        assertEquals(new BigDecimal("12.35"), persisted.getPrecoEmbalagem());
        assertTrue(persisted.getAtivo());
    }

    @Test
    void update_shouldApplyChanges() {
        Material existing = material(5L, "X", true);
        when(repo.findById(5L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Material input = material(null, "Novo", false);
        input.setVolumeEmbalagem(new BigDecimal("20"));
        input.setPrecoEmbalagem(new BigDecimal("100"));

        Optional<Material> updatedOpt = service.update(5L, input);
        assertTrue(updatedOpt.isPresent());
        Material updated = updatedOpt.get();
        assertEquals("Novo", updated.getProduto());
        assertEquals(new BigDecimal("20.00"), updated.getVolumeEmbalagem());
        assertEquals(new BigDecimal("100.00"), updated.getPrecoEmbalagem());
        assertFalse(updated.getAtivo());
    }

    @Test
    void delete_shouldReturnTrueWhenExists() {
        when(repo.existsById(7L)).thenReturn(true);
        doNothing().when(repo).deleteById(7L);
        assertTrue(service.delete(7L));
        verify(repo).deleteById(7L);
    }

    @Test
    void toggleStatus_shouldFlipActive() {
        Material existing = material(3L, "A", true);
        when(repo.findById(3L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Material> saved = service.toggleStatus(3L, false);
        assertTrue(saved.isPresent());
        assertFalse(saved.get().getAtivo());
    }
}
