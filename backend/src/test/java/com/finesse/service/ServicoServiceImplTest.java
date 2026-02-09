package com.finesse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.finesse.dto.PrecoSetResponse;
import com.finesse.entity.PrecoPraticado;
import com.finesse.entity.Servico;
import com.finesse.repository.PrecoPraticadoRepository;
import com.finesse.repository.ServicoRepository;

class ServicoServiceImplTest {

    @Mock
    ServicoRepository servicoRepository;

    @Mock
    PrecoPraticadoRepository precoRepository;

    @InjectMocks
    ServicoServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Servico servico(Long id) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome("Srv");
        s.setGrupo("G");
        s.setDuracaoMinutos(30);
        s.setAtivo(true);
        return s;
    }

    @Test
    void definirPreco_shouldIgnoreWhenEqualToVigente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico(1L)));
        PrecoPraticado vigente = new PrecoPraticado(servico(1L), new BigDecimal("100.00"), LocalDate.now().minusDays(10));
        when(precoRepository.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(1L)).thenReturn(Optional.of(vigente));

        PrecoSetResponse resp = service.definirPreco(1L, new BigDecimal("100"));
        assertFalse(resp.changed());
        assertEquals(new BigDecimal("100.00"), resp.vigente().getPreco());
        verify(precoRepository, never()).save(any(PrecoPraticado.class));
    }

    @Test
    void definirPreco_shouldScheduleNextDayWhenSameStartDate() {
        when(servicoRepository.findById(2L)).thenReturn(Optional.of(servico(2L)));
        LocalDate hoje = LocalDate.now();
        PrecoPraticado vigente = new PrecoPraticado(servico(2L), new BigDecimal("50.00"), hoje);
        when(precoRepository.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(2L)).thenReturn(Optional.of(vigente));
        when(precoRepository.save(any())).thenAnswer(inv -> {
            PrecoPraticado p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PrecoSetResponse resp = service.definirPreco(2L, new BigDecimal("75.00"));
        assertTrue(resp.changed());

        ArgumentCaptor<PrecoPraticado> captor = ArgumentCaptor.forClass(PrecoPraticado.class);
        verify(precoRepository, times(2)).save(captor.capture());
        PrecoPraticado fechado = captor.getAllValues().get(0);
        PrecoPraticado novo = captor.getAllValues().get(1);

        assertFalse(fechado.isVigente());
        assertEquals(hoje, fechado.getVigenciaFim());
        assertEquals(new BigDecimal("75.00"), novo.getPreco());
        assertEquals(hoje.plusDays(1), novo.getVigenciaInicio());
        assertTrue(novo.isVigente());
    }

    @Test
    void definirPreco_shouldCreateNewWhenNoVigente() {
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(servico(3L)));
        when(precoRepository.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(3L)).thenReturn(Optional.empty());
        when(precoRepository.save(any())).thenAnswer(inv -> {
            PrecoPraticado p = inv.getArgument(0);
            p.setId(11L);
            return p;
        });

        PrecoSetResponse resp = service.definirPreco(3L, new BigDecimal("120.00"));
        assertTrue(resp.changed());
        assertEquals(new BigDecimal("120.00"), resp.vigente().getPreco());
        assertEquals(LocalDate.now(), resp.vigente().getVigenciaInicio());
        assertTrue(resp.vigente().isVigente());
    }
}
