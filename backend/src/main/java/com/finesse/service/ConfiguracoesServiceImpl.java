package com.finesse.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Configuracoes;
import com.finesse.exception.ServiceOperationException;
import com.finesse.repository.ConfiguracoesRepository;

@Service
public class ConfiguracoesServiceImpl implements ConfiguracoesService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracoesServiceImpl.class);

    @Autowired
    private ConfiguracoesRepository repo;

    @Override
    public Page<Configuracoes> list(Boolean ativo, int page, int size, String sort) {
        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), parseSort(sort));
            if (ativo != null) {
                return repo.findByAtivo(ativo, pageable);
            }
            return repo.findAll(pageable);
        } catch (Exception ex) {
            log.error("Falha ao listar configurações", ex);
            throw new ServiceOperationException("Falha ao listar configurações", ex);
        }
    }

    @Override
    public Optional<Configuracoes> findById(Long id) {
        try {
            return repo.findById(id);
        } catch (Exception ex) {
            log.error("Falha ao buscar configuração id={}", id, ex);
            throw new ServiceOperationException("Falha ao buscar configuração", ex);
        }
    }

    @Override
    @Transactional
    public Configuracoes create(Configuracoes c) {
        try {
            sanitize(c);
            Configuracoes saved = repo.save(c);
            log.info("Configuração criada id={}", saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao criar configuração", ex);
            throw new ServiceOperationException("Falha ao criar configuração", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Configuracoes> update(Long id, Configuracoes input) {
        try {
            return repo.findById(id).map(existing -> {
                existing.setPretensaoSalarialMensal(input.getPretensaoSalarialMensal());
                existing.setHorasSemanais(input.getHorasSemanais());
                existing.setSemanasMediaMes(input.getSemanasMediaMes());
                existing.setCustoFixoPct(input.getCustoFixoPct());
                existing.setMargemLucroPadraoPct(input.getMargemLucroPadraoPct());
                existing.setAtualizadoEm(java.time.LocalDateTime.now());
                existing.setAtivo(input.isAtivo());
                sanitize(existing);
                Configuracoes saved = repo.save(existing);
                log.info("Configuração atualizada id={}", saved.getId());
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao atualizar configuração id={}", id, ex);
            throw new ServiceOperationException("Falha ao atualizar configuração", ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            if (repo.existsById(id)) {
                repo.deleteById(id);
                log.info("Configuração removida id={}", id);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error("Falha ao remover configuração id={}", id, ex);
            throw new ServiceOperationException("Falha ao remover configuração", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Configuracoes> toggleStatus(Long id, boolean ativo) {
        try {
            return repo.findById(id).map(c -> {
                c.setAtivo(ativo);
                c.setAtualizadoEm(java.time.LocalDateTime.now());
                Configuracoes saved = repo.save(c);
                log.info("Status da configuração alterado id={} ativo={}", saved.getId(), ativo);
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao alternar status de configuração id={}", id, ex);
            throw new ServiceOperationException("Falha ao alternar status de configuração", ex);
        }
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by("id").ascending();
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        Sort base = Sort.by(prop);
        return "desc".equals(dir) ? base.descending() : base.ascending();
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
