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

import com.finesse.entity.Material;
import com.finesse.repository.MaterialRepository;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public Page<Material> list(String produto, Boolean ativo, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(parseSort(sort)));
        if (produto != null && !produto.isBlank() && ativo != null) {
            return materialRepository.findByProdutoContainingIgnoreCaseAndAtivo(produto.trim(), ativo, pageable);
        } else if (produto != null && !produto.isBlank()) {
            return materialRepository.findByProdutoContainingIgnoreCase(produto.trim(), pageable);
        } else if (ativo != null) {
            return materialRepository.findByAtivo(ativo, pageable);
        }
        return materialRepository.findAll(pageable);
    }

    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Transactional
    public Material create(Material m) {
        sanitize(m);
        validateBusiness(m);
        return materialRepository.save(m);
    }

    @Transactional
    public Optional<Material> update(Long id, Material input) {
        return materialRepository.findById(id).map(existing -> {
            existing.setProduto(trim(input.getProduto()));
            existing.setUnidadeMedida(input.getUnidadeMedida());
            existing.setVolumeEmbalagem(input.getVolumeEmbalagem());
            existing.setPrecoEmbalagem(input.getPrecoEmbalagem());
            existing.setCustoUnitario(input.getCustoUnitario());
            existing.setObservacoes(trim(input.getObservacoes()));
            existing.setAtivo(input.getAtivo() == null ? existing.getAtivo() : input.getAtivo());
            sanitize(existing);
            validateBusiness(existing);
            return materialRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (materialRepository.existsById(id)) {
            materialRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Material> toggleStatus(Long id, boolean ativo) {
        return materialRepository.findById(id).map(m -> {
            m.setAtivo(ativo);
            return materialRepository.save(m);
        });
    }

    private String parseSort(String sort) {
        if (sort == null || sort.isBlank()) return "id";
        return sort;
    }

    private void sanitize(Material m) {
        m.setProduto(trim(m.getProduto()));
        m.setObservacoes(trim(m.getObservacoes()));
        // Garantir valores positivos e escala
        if (m.getVolumeEmbalagem() != null)
            m.setVolumeEmbalagem(m.getVolumeEmbalagem().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (m.getPrecoEmbalagem() != null)
            m.setPrecoEmbalagem(m.getPrecoEmbalagem().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (m.getCustoUnitario() != null)
            m.setCustoUnitario(m.getCustoUnitario().setScale(6, java.math.RoundingMode.HALF_UP));
        if (m.getAtivo() == null) m.setAtivo(Boolean.TRUE);
    }

    private void validateBusiness(Material m) {
        // Regra de negócio: custo unitário deve ser preco/volume quando ambos presentes
        if (m.getPrecoEmbalagem() != null && m.getVolumeEmbalagem() != null &&
            m.getVolumeEmbalagem().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal calc = m.getPrecoEmbalagem().divide(m.getVolumeEmbalagem(), 6, java.math.RoundingMode.HALF_UP);
            m.setCustoUnitario(calc);
        }
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}

