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

import com.finesse.entity.Atividade;
import com.finesse.exception.ServiceOperationException;
import com.finesse.repository.AtividadeRepository;

@Service
public class AtividadeServiceImpl implements AtividadeService {

    private static final Logger log = LoggerFactory.getLogger(AtividadeServiceImpl.class);

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Override
    public Page<Atividade> list(String titulo, Boolean ativo, int page, int size, String sort) {
        try {
            int page0 = Math.max(0, page);
            int size1 = Math.max(1, size);

            boolean hasTitulo = titulo != null && !titulo.isBlank();
            if (hasTitulo) {
                List<Atividade> all = atividadeRepository.findAll();
                Stream<Atividade> stream = all.stream();

                String q = normalize(titulo != null ? titulo.trim() : null);
                stream = stream.filter(a -> {
                    String p = normalize(a.getNome());
                    return p != null && p.contains(q);
                });
                if (ativo != null) {
                    boolean target = Boolean.TRUE.equals(ativo);
                    stream = stream.filter(a -> Boolean.TRUE.equals(a.isAtivo()) == target);
                }

                Comparator<Atividade> comparator = buildComparator(sort);
                List<Atividade> filteredSorted = comparator != null ? stream.sorted(comparator).toList() : stream.toList();

                int total = filteredSorted.size();
                int from = Math.min(page0 * size1, total);
                int to = Math.min(from + size1, total);
                List<Atividade> content = filteredSorted.subList(from, to);

                Pageable pageableMeta = PageRequest.of(page0, size1, parseSort(sort));
                return new PageImpl<>(content, pageableMeta, total);
            } else {
                Pageable pageable = PageRequest.of(page0, size1, parseSort(sort));
                if (ativo != null) {
                    return atividadeRepository.findByAtivo(ativo, pageable);
                }
                return atividadeRepository.findAll(pageable);
            }
        } catch (Exception ex) {
            log.error("Falha ao listar atividades", ex);
            throw new ServiceOperationException("Falha ao listar atividades", ex);
        }
    }

    @Override
    public Optional<Atividade> findById(Long id) {
        try {
            return atividadeRepository.findById(id);
        } catch (Exception ex) {
            log.error("Falha ao buscar atividade id={}", id, ex);
            throw new ServiceOperationException("Falha ao buscar atividade", ex);
        }
    }

    @Override
    @Transactional
    public Atividade create(Atividade a) {
        try {
            sanitize(a);
            Atividade saved = atividadeRepository.save(a);
            log.info("Atividade criada id={}", saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao criar atividade", ex);
            throw new ServiceOperationException("Falha ao criar atividade", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Atividade> update(Long id, Atividade input) {
        try {
            return atividadeRepository.findById(id).map(existing -> {
                existing.setNome(trim(input.getNome()));
                existing.setObservacao(trim(input.getObservacao()));
                existing.setCnae(trim(input.getCnae()));
                existing.setAliquotaTotalPct(input.getAliquotaTotalPct());
                existing.setIssPct(input.getIssPct());
                existing.setAtivo(input.isAtivo());
                sanitize(existing);
                Atividade saved = atividadeRepository.save(existing);
                log.info("Atividade atualizada id={}", saved.getId());
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao atualizar atividade id={}", id, ex);
            throw new ServiceOperationException("Falha ao atualizar atividade", ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            if (atividadeRepository.existsById(id)) {
                atividadeRepository.deleteById(id);
                log.info("Atividade removida id={}", id);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error("Falha ao remover atividade id={}", id, ex);
            throw new ServiceOperationException("Falha ao remover atividade", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Atividade> toggleStatus(Long id, boolean ativo) {
        try {
            return atividadeRepository.findById(id).map(a -> {
                a.setAtivo(ativo);
                Atividade saved = atividadeRepository.save(a);
                log.info("Status da atividade alterado id={} ativo={}", saved.getId(), ativo);
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao alternar status da atividade id={}", id, ex);
            throw new ServiceOperationException("Falha ao alternar status da atividade", ex);
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

    private Comparator<Atividade> buildComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(Atividade::getId, Comparator.nullsLast(Long::compareTo));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<Atividade> cmp;
        switch (prop) {
            case "id":
                cmp = Comparator.comparing(Atividade::getId, Comparator.nullsLast(Long::compareTo));
                break;
            case "nome":
                cmp = Comparator.comparing(
                        a -> normalize(a.getNome()),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "cnae":
                cmp = Comparator.comparing(
                        a -> a.getCnae(),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "aliquotaTotalPct":
                cmp = Comparator.comparing(Atividade::getAliquotaTotalPct, Comparator.nullsLast(java.math.BigDecimal::compareTo));
                break;
            case "issPct":
                cmp = Comparator.comparing(Atividade::getIssPct, Comparator.nullsLast(java.math.BigDecimal::compareTo));
                break;
            case "ativo":
                cmp = Comparator.comparing(Atividade::isAtivo);
                break;
            default:
                cmp = Comparator.comparing(Atividade::getId, Comparator.nullsLast(Long::compareTo));
        }
        return desc ? cmp.reversed() : cmp;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String lower = s.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void sanitize(Atividade a) {
        a.setNome(trim(a.getNome()));
        a.setObservacao(trim(a.getObservacao()));
        a.setCnae(trim(a.getCnae()));
        if (a.getAliquotaTotalPct() != null)
            a.setAliquotaTotalPct(a.getAliquotaTotalPct().abs().setScale(4, java.math.RoundingMode.HALF_UP));
        if (a.getIssPct() != null)
            a.setIssPct(a.getIssPct().abs().setScale(4, java.math.RoundingMode.HALF_UP));
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}
