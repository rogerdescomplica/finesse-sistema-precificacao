package com.finesse.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.finesse.dto.PrecoSetResponse;
import com.finesse.entity.PrecoPraticado;
import com.finesse.entity.Servico;

public interface ServicoService {
    Page<Servico> list(String nome, String grupo, Boolean ativo, int page, int size, String sort);
    Optional<Servico> findById(Long id);
    Servico create(Servico s);
    Optional<Servico> update(Long id, Servico input);
    Optional<Servico> toggleStatus(Long id, boolean ativo);
    boolean delete(Long id);
    Optional<PrecoPraticado> vigente(Long servicoId);
    PrecoSetResponse definirPreco(Long servicoId, BigDecimal novoPreco);
}
