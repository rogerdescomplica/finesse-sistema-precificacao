package com.finesse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finesse.entity.Configuracoes;

@Repository
public interface ConfiguracoesRepository extends JpaRepository<Configuracoes, Long> {
    Page<Configuracoes> findByAtivo(Boolean ativo, Pageable pageable);
}
