package com.finesse.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Configuracoes;
import com.finesse.repository.ConfiguracoesRepository;

@Service
public class ConfiguracoesService {

    @Autowired
    private ConfiguracoesRepository repo;

    public Page<Configuracoes> list(Boolean ativo, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(parseSort(sort)));
        if (ativo != null) {
            return repo.findByAtivo(ativo, pageable);
        }
        return repo.findAll(pageable);
    }

    public Optional<Configuracoes> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Configuracoes create(Configuracoes c) {
        sanitize(c);
        return repo.save(c);
    }

    @Transactional
    public Optional<Configuracoes> update(Long id, Configuracoes input) {
        return repo.findById(id).map(existing -> {
            existing.setPretensaoSalarialMensal(input.getPretensaoSalarialMensal());
            existing.setHorasSemanais(input.getHorasSemanais());
            existing.setSemanasMediaMes(input.getSemanasMediaMes());
            existing.setCustoFixoPct(input.getCustoFixoPct());
            existing.setMargemLucroPadraoPct(input.getMargemLucroPadraoPct());
            existing.setAtualizadoEm(java.time.LocalDateTime.now());
            existing.setAtivo(input.isAtivo());
            sanitize(existing);
            return repo.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Configuracoes> toggleStatus(Long id, boolean ativo) {
        return repo.findById(id).map(c -> {
            c.setAtivo(ativo);
            c.setAtualizadoEm(java.time.LocalDateTime.now());
            return repo.save(c);
        });
    }

    private String parseSort(String sort) {
        if (sort == null || sort.isBlank()) return "id";
        return sort;
    }

    private void sanitize(Configuracoes c) {
        if (c.getPretensaoSalarialMensal() != null)
            c.setPretensaoSalarialMensal(c.getPretensaoSalarialMensal().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (c.getHorasSemanais() != null)
            c.setHorasSemanais(c.getHorasSemanais().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (c.getSemanasMediaMes() != null)
            c.setSemanasMediaMes(c.getSemanasMediaMes().abs().setScale(4, java.math.RoundingMode.HALF_UP));
        if (c.getCustoFixoPct() != null)
            c.setCustoFixoPct(c.getCustoFixoPct().abs().setScale(4, java.math.RoundingMode.HALF_UP));
        if (c.getMargemLucroPadraoPct() != null)
            c.setMargemLucroPadraoPct(c.getMargemLucroPadraoPct().abs().setScale(4, java.math.RoundingMode.HALF_UP));
    }
}
