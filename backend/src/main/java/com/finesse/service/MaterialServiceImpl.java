package com.finesse.service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Material;
import com.finesse.exception.ServiceOperationException;
import com.finesse.exception.ValidationException;
import com.finesse.repository.MaterialRepository;

@Service
public class MaterialServiceImpl implements MaterialService {

    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public Page<Material> list(String produto, Boolean ativo, int page, int size, String sort) {
        try {
            int page0 = Math.max(0, page);
            int size1 = Math.max(1, size);

            boolean hasProduto = produto != null && !produto.isBlank();
            if (hasProduto) {
                List<Material> all = materialRepository.findAll();
                Stream<Material> stream = all.stream();

                String q = normalize(produto != null ? produto.trim() : null);
                stream = stream.filter(m -> {
                    String p = normalize(m.getProduto());
                    return p != null && p.contains(q);
                });
                if (ativo != null) {
                    boolean target = Boolean.TRUE.equals(ativo);
                    stream = stream.filter(m -> Boolean.TRUE.equals(m.getAtivo()) == target);
                }

                Comparator<Material> comparator = buildComparator(sort);
                List<Material> filteredSorted = comparator != null ? stream.sorted(comparator).toList() : stream.toList();

                int total = filteredSorted.size();
                int from = Math.min(page0 * size1, total);
                int to = Math.min(from + size1, total);
                List<Material> content = filteredSorted.subList(from, to);

                Pageable pageableMeta = PageRequest.of(page0, size1, parseSort(sort));
                return new PageImpl<>(content, pageableMeta, total);
            } else {
                Pageable pageable = PageRequest.of(page0, size1, parseSort(sort));
                if (ativo != null) {
                    return materialRepository.findByAtivo(ativo, pageable);
                }
                return materialRepository.findAll(pageable);
            }
        } catch (Exception ex) {
            log.error("Falha ao listar materiais", ex);
            throw new ServiceOperationException("Falha ao listar materiais", ex);
        }

    }

    @Override
    public Optional<Material> findById(Long id) {
        try {
            return materialRepository.findById(id);
        } catch (Exception ex) {
            log.error("Falha ao buscar material id={}", id, ex);
            throw new ServiceOperationException("Falha ao buscar material", ex);
        }
    }

    @Override
    @Transactional
    public Material create(Material m) {
        try {
            sanitize(m);
            validateBusiness(m);
            Material saved = materialRepository.save(m);
            log.info("Material criado id={}", saved.getId());
            return saved;
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao criar material: {}", vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao criar material", ex);
            throw new ServiceOperationException("Falha ao criar material", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Material> update(Long id, Material input) {
        try {
            return materialRepository.findById(id).map(existing -> {
                existing.setProduto(trim(input.getProduto()));
                existing.setUnidadeMedida(input.getUnidadeMedida());
                existing.setVolumeEmbalagem(input.getVolumeEmbalagem());
                existing.setPrecoEmbalagem(input.getPrecoEmbalagem());
                existing.setObservacoes(trim(input.getObservacoes()));
                existing.setAtivo(input.getAtivo() == null ? existing.getAtivo() : input.getAtivo());
                sanitize(existing);
                validateBusiness(existing);
                Material saved = materialRepository.save(existing);
                log.info("Material atualizado id={}", saved.getId());
                return saved;
            });
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao atualizar material id={}: {}", id, vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao atualizar material id={}", id, ex);
            throw new ServiceOperationException("Falha ao atualizar material", ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            if (materialRepository.existsById(id)) {
                materialRepository.deleteById(id);
                log.info("Material removido id={}", id);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error("Falha ao remover material id={}", id, ex);
            throw new ServiceOperationException("Falha ao remover material", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Material> toggleStatus(Long id, boolean ativo) {
        try {
            return materialRepository.findById(id).map(m -> {
                m.setAtivo(ativo);
                Material saved = materialRepository.save(m);
                log.info("Status do material alterado id={} ativo={}", saved.getId(), ativo);
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao alternar status do material id={}", id, ex);
            throw new ServiceOperationException("Falha ao alternar status do material", ex);
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

    private Comparator<Material> buildComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(Material::getId, Comparator.nullsLast(Long::compareTo));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<Material> cmp;
        switch (prop) {
            case "id":
                cmp = Comparator.comparing(Material::getId, Comparator.nullsLast(Long::compareTo));
                break;
            case "produto":
                cmp = Comparator.comparing(
                        m -> normalize(m.getProduto()),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "unidadeMedida":
                cmp = Comparator.comparing(
                        m -> m.getUnidadeMedida() == null ? null : m.getUnidadeMedida().name(),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "volumeEmbalagem":
                cmp = Comparator.comparing(Material::getVolumeEmbalagem, Comparator.nullsLast(java.math.BigDecimal::compareTo));
                break;
            case "precoEmbalagem":
                cmp = Comparator.comparing(Material::getPrecoEmbalagem, Comparator.nullsLast(java.math.BigDecimal::compareTo));
                break;
            case "custoUnitario":
                cmp = Comparator.comparing(Material::getCustoUnitario, Comparator.nullsLast(java.math.BigDecimal::compareTo));
                break;
            case "ativo":
                cmp = Comparator.comparing(Material::getAtivo, Comparator.nullsLast(Boolean::compareTo));
                break;
            default:
                cmp = Comparator.comparing(Material::getId, Comparator.nullsLast(Long::compareTo));
        }
        return desc ? cmp.reversed() : cmp;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String lower = s.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void sanitize(Material m) {
        m.setProduto(trim(m.getProduto()));
        m.setObservacoes(trim(m.getObservacoes()));
        if (m.getVolumeEmbalagem() != null)
            m.setVolumeEmbalagem(m.getVolumeEmbalagem().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (m.getPrecoEmbalagem() != null)
            m.setPrecoEmbalagem(m.getPrecoEmbalagem().abs().setScale(2, java.math.RoundingMode.HALF_UP));
        if (m.getAtivo() == null) m.setAtivo(Boolean.TRUE);
    }

    private void validateBusiness(Material m) {
        // Validações de domínio adicionais podem ser adicionadas aqui
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}
