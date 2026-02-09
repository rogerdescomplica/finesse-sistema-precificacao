package com.finesse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finesse.entity.Atividade;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    Page<Atividade> findByAtivo(Boolean ativo, Pageable pageable);

}
