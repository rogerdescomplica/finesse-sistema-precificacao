package com.finesse.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

import com.finesse.dto.PrecoSetResponse;
import com.finesse.entity.Atividade;
import com.finesse.entity.Servico;
import com.finesse.entity.Material;
import com.finesse.entity.ServicoMaterial;
import com.finesse.entity.PrecoPraticado;
import com.finesse.exception.ServiceOperationException;
import com.finesse.exception.ValidationException;
import com.finesse.repository.ServicoRepository;
import com.finesse.repository.PrecoPraticadoRepository;


@Service
public class ServicoServiceImpl implements ServicoService {

    private static final Logger log = LoggerFactory.getLogger(ServicoServiceImpl.class);

    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private PrecoPraticadoRepository precoRepository;
    @Autowired
    private AtividadeService atividadeService;
    @Autowired
    private MaterialService materialService;

    @Override
    public Page<Servico> list(String nome, String grupo, Boolean ativo, int page, int size, String sort) {
        try {
            int page0 = Math.max(0, page);
            int size1 = Math.max(1, size);

            boolean hasNome = nome != null && !nome.isBlank();
            boolean hasGrupo = grupo != null && !grupo.isBlank();

            if (hasNome || hasGrupo) {
                List<Servico> all = servicoRepository.findAll();
                Stream<Servico> stream = all.stream();

                if (hasNome) {
                    String q = normalize(Optional.ofNullable(nome).orElse("").trim());
                    stream = stream.filter(s -> {
                        String n = normalize(Optional.ofNullable(s.getNome()).orElse(""));
                        return n != null && n.contains(q);
                    });
                }
                if (hasGrupo) {
                    String qg = normalize(Optional.ofNullable(grupo).orElse("").trim());
                    stream = stream.filter(s -> {
                        String g = normalize(Optional.ofNullable(s.getGrupo()).orElse(""));
                        return g != null && g.contains(qg);
                    });
                }
                if (ativo != null) {
                    boolean target = Boolean.TRUE.equals(ativo);
                    stream = stream.filter(s -> Boolean.TRUE.equals(s.isAtivo()) == target);
                }

                Comparator<Servico> comparator = buildComparator(sort);
                List<Servico> filteredSorted = comparator != null ? stream.sorted(comparator).toList() : stream.toList();

                int total = filteredSorted.size();
                int from = Math.min(page0 * size1, total);
                int to = Math.min(from + size1, total);
                List<Servico> content = filteredSorted.subList(from, to);

                Pageable pageableMeta = PageRequest.of(page0, size1, parseSort(sort));
                return new PageImpl<>(content, pageableMeta, total);
            } else {
                Pageable pageable = PageRequest.of(page0, size1, parseSort(sort));
                if (ativo != null) {
                    return servicoRepository.findByAtivo(ativo, pageable);
                }
                return servicoRepository.findAll(pageable);
            }
        } catch (Exception ex) {
            log.error("Falha ao listar serviços", ex);
            throw new ServiceOperationException("Falha ao listar serviços", ex);
        }
    }

    @Override
    public Optional<Servico> findById(Long id) {
        try {
            return servicoRepository.findById(id);
        } catch (Exception ex) {
            log.error("Falha ao buscar serviço id={}", id, ex);
            throw new ServiceOperationException("Falha ao buscar serviço", ex);
        }
    }

    @Override
    @Transactional
    public Servico create(Servico s) {
        try {
            validarCarregarAtividade(s);
            validarCarregarMateriais(s);
            normalizarDadosServico(s);
            validarCamposObrigatorios(s);
            Servico saved = servicoRepository.save(s);
            if (s.getPrecoPraticadoInput() != null) {
                definirPreco(saved.getId(), s.getPrecoPraticadoInput());
            }
            log.info("Serviço criado id={}", saved.getId());
            return saved;
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao criar serviço: {}", vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao criar serviço", ex);
            throw new ServiceOperationException("Falha ao criar serviço", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Servico> update(Long id, Servico input) {
        try {
            return servicoRepository.findById(id).map(existing -> {
                existing.setNome(trim(input.getNome()));
                existing.setGrupo(trim(input.getGrupo()));
                existing.setDuracaoMinutos(input.getDuracaoMinutos());
                existing.setMargemLucroCustomPct(input.getMargemLucroCustomPct());
                existing.setAtivo(input.isAtivo());
                // atividade
                if (input.getAtividade() != null) {
                    Long atividadeId = input.getAtividade().getId();
                    if (atividadeId != null) {
                        Atividade atv = atividadeService.findById(atividadeId)
                                .orElseThrow(() -> new ValidationException("Atividade inválida"));
                        existing.setAtividade(atv);
                    }
                }
                // materiais (merge: atualiza existentes, adiciona novos, remove ausentes)
                if (input.getMateriais() != null) {
                    Map<Long, ServicoMaterial> byMatId = new HashMap<>();
                    for (ServicoMaterial sm : existing.getMateriais()) {
                        Material m = sm.getMaterial();
                        Long mid = m != null ? m.getId() : null;
                        if (mid != null) byMatId.put(mid, sm);
                    }
                    Set<Long> seen = new HashSet<>();
                    for (ServicoMaterial smIn : input.getMateriais()) {
                        if (smIn == null) continue;
                        Material mIn = smIn.getMaterial();
                        Long mid = mIn != null ? mIn.getId() : null;
                        if (mid == null) throw new ValidationException("Material inválido");
                        Material mat = materialService.findById(mid)
                                .orElseThrow(() -> new ValidationException("Material não encontrado"));
                        BigDecimal qtd = smIn.getQuantidadeUsada();
                        if (qtd == null || qtd.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new ValidationException("Quantidade do material deve ser positiva");
                        }
                        BigDecimal qAdj = qtd.abs().setScale(4, RoundingMode.HALF_UP);
                        ServicoMaterial existingSm = byMatId.get(mid);
                        if (existingSm != null) {
                            existingSm.setMaterial(mat);
                            existingSm.setQuantidadeUsada(qAdj);
                        } else {
                            ServicoMaterial newSm = new ServicoMaterial(existing, mat, qAdj);
                            existing.getMateriais().add(newSm);
                        }
                        seen.add(mid);
                    }
                    existing.getMateriais().removeIf(sm -> {
                        Material m = sm.getMaterial();
                        Long mid = m != null ? m.getId() : null;
                        return mid != null && !seen.contains(mid);
                    });
                }
                normalizarDadosServico(existing);
                validarCamposObrigatorios(existing);
                Servico saved = servicoRepository.save(existing);
                if (input.getPrecoPraticadoInput() != null) {
                    definirPreco(saved.getId(), input.getPrecoPraticadoInput());
                }
                log.info("Serviço atualizado id={}", saved.getId());
                return saved;
            });
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao atualizar serviço id={}: {}", id, vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao atualizar serviço id={}", id, ex);
            throw new ServiceOperationException("Falha ao atualizar serviço", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Servico> toggleStatus(Long id, boolean ativo) {
        try {
            return servicoRepository.findById(id).map(s -> {
                s.setAtivo(ativo);
                Servico saved = servicoRepository.save(s);
                log.info("Status do serviço alterado id={} ativo={}", saved.getId(), ativo);
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao alternar status do serviço id={}", id, ex);
            throw new ServiceOperationException("Falha ao alternar status do serviço", ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            if (servicoRepository.existsById(id)) {
                servicoRepository.deleteById(id);
                log.info("Serviço removido id={}", id);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.error("Falha ao remover serviço id={}", id, ex);
            throw new ServiceOperationException("Falha ao remover serviço", ex);
        }
    }

    private void validarCarregarAtividade(Servico s) {
        if (s.getAtividade() == null) throw new ValidationException("Atividade é obrigatória");
        Long atividadeId = s.getAtividade().getId();
        if (atividadeId == null) throw new ValidationException("Atividade é obrigatória");
        Atividade atv = atividadeService.findById(atividadeId)
                .orElseThrow(() -> new ValidationException("Atividade inválida"));
        s.setAtividade(atv);
    }

    private void validarCarregarMateriais(Servico s) {
        List<ServicoMaterial> list = s.getMateriais();
        if (list == null) return;
        java.util.ArrayList<ServicoMaterial> rebuilt = new java.util.ArrayList<>();
        for (ServicoMaterial sm : list) {
            if (sm == null) continue;
            Material m = sm.getMaterial();
            Long mid = m != null ? m.getId() : null;
            if (mid == null) throw new ValidationException("Material inválido");
            Material mat = materialService.findById(mid)
                    .orElseThrow(() -> new ValidationException("Material não encontrado"));
            BigDecimal qtd = sm.getQuantidadeUsada();
            if (qtd == null || qtd.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Quantidade do material deve ser positiva");
            }
            BigDecimal qAdj = qtd.abs().setScale(4, RoundingMode.HALF_UP);
            ServicoMaterial newSm = new ServicoMaterial(s, mat, qAdj);
            rebuilt.add(newSm);
        }
        s.setMateriais(rebuilt);
    }

    private void normalizarDadosServico(Servico s) {
        s.setNome(trim(s.getNome()));
        s.setGrupo(trim(s.getGrupo()));
        if (s.getDuracaoMinutos() != null) {
            s.setDuracaoMinutos(Math.max(1, s.getDuracaoMinutos()));
        }
        if (s.getMargemLucroCustomPct() != null) {
            BigDecimal v = s.getMargemLucroCustomPct().abs().setScale(4, RoundingMode.HALF_UP);
            s.setMargemLucroCustomPct(v);
        }
    }

    private void validarCamposObrigatorios(Servico s) {
        if (s.getNome() == null || s.getNome().isBlank())
            throw new ValidationException("Nome do serviço é obrigatório");
        if (s.getGrupo() == null || s.getGrupo().isBlank())
            throw new ValidationException("Categoria do serviço (grupo) é obrigatória");
        if (s.getDuracaoMinutos() == null || s.getDuracaoMinutos() <= 0)
            throw new ValidationException("Duração (minutos) deve ser positiva");
        if (s.getAtividade() == null)
            throw new ValidationException("Atividade é obrigatória");
    }

    @Override
    public Optional<PrecoPraticado> vigente(Long servicoId) {
        try {
            return precoRepository.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(servicoId);
        } catch (Exception ex) {
            log.error("Falha ao buscar preço vigente para serviço id={}", servicoId, ex);
            throw new ServiceOperationException("Falha ao buscar preço vigente", ex);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public PrecoSetResponse definirPreco(Long servicoId, BigDecimal novoPreco) {
        try {
            if (servicoId == null) throw new ValidationException("Serviço é obrigatório");
            Servico servico = servicoRepository.findById(servicoId).orElseThrow(() -> new ValidationException("Serviço não encontrado"));
            if (novoPreco == null) throw new ValidationException("Preço é obrigatório");
            BigDecimal valorArrendodado = obterAbsolutoArredondado(novoPreco);
            if (valorArrendodado.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Preço deve ser maior ou igual a zero");
            if (valorArrendodado.compareTo(new BigDecimal("1000000")) > 0) throw new ValidationException("Preço excede o limite permitido");
            LocalDate ini = LocalDate.now();

            Optional<PrecoPraticado> atualOpt = vigente(servicoId);
            if (atualOpt.isPresent()) {
                PrecoPraticado atual = atualOpt.get();
                if (atual.getPreco() != null && obterAbsolutoArredondado(atual.getPreco()).compareTo(valorArrendodado) == 0) {
                    return new PrecoSetResponse(false, atual);
                }
                atual.setVigente(false);
                atual.setVigenciaFim(ini);
                precoRepository.save(atual);
            }

            PrecoPraticado novo = new PrecoPraticado(servico, valorArrendodado, ini);
            novo.setVigente(true);
            novo.setVigenciaFim(null);
            PrecoPraticado saved = precoRepository.save(novo);
            return new PrecoSetResponse(true, saved);
        } catch (ValidationException vex) {
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao definir preço praticado para serviço id={}", servicoId, ex);
            throw new ServiceOperationException("Falha ao definir preço praticado", ex);
        }
    }

    private BigDecimal obterAbsolutoArredondado(BigDecimal v) {
        return v.abs().setScale(2, RoundingMode.HALF_UP);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by("id").ascending();
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        Sort base = Sort.by(prop);
        return "desc".equals(dir) ? base.descending() : base.ascending();
    }

    private Comparator<Servico> buildComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(Servico::getId, Comparator.nullsLast(Long::compareTo));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<Servico> cmp;
        switch (prop) {
            case "id":
                cmp = Comparator.comparing(Servico::getId, Comparator.nullsLast(Long::compareTo));
                break;
            case "nome":
                cmp = Comparator.comparing(
                        s -> normalize(s.getNome()),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "grupo":
                cmp = Comparator.comparing(
                        s -> normalize(s.getGrupo()),
                        Comparator.nullsLast(String::compareTo)
                );
                break;
            case "duracaoMinutos":
                cmp = Comparator.comparing(Servico::getDuracaoMinutos, Comparator.nullsLast(Integer::compareTo));
                break;
            case "ativo":
                cmp = Comparator.comparing(Servico::isAtivo, Comparator.nullsLast(Boolean::compareTo));
                break;
            default:
                cmp = Comparator.comparing(Servico::getId, Comparator.nullsLast(Long::compareTo));
        }
        return desc ? cmp.reversed() : cmp;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String lower = s.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}
