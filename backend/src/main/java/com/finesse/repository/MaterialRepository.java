package com.finesse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finesse.entity.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Page<Material> findByProdutoContainingIgnoreCase(String produto, Pageable pageable);
    Page<Material> findByAtivo(Boolean ativo, Pageable pageable);
    Page<Material> findByProdutoContainingIgnoreCaseAndAtivo(String produto, Boolean ativo, Pageable pageable);
}

