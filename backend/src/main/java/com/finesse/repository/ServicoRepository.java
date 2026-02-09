package com.finesse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finesse.entity.Servico;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Page<Servico> findByAtivo(Boolean ativo, Pageable pageable);
    Page<Servico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Servico> findByGrupoContainingIgnoreCase(String grupo, Pageable pageable);
    Page<Servico> findByNomeContainingIgnoreCaseAndAtivo(String nome, Boolean ativo, Pageable pageable);
    Page<Servico> findByGrupoContainingIgnoreCaseAndAtivo(String grupo, Boolean ativo, Pageable pageable);
}
