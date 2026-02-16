package com.finesse.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.finesse.dto.PageResponse;
import com.finesse.dto.PrecoAtualResponse;
import com.finesse.dto.ServicoDetailResponse;
import com.finesse.entity.Servico;
import com.finesse.service.ServicoService;
import com.finesse.repository.PrecoPraticadoRepository;
import com.finesse.repository.ServicoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Serviços", description = "CRUD de Serviços")
@RequestMapping({"/api/servico", "/api/servicos"})
@Validated
public class ServicoController {

    @Autowired
    private ServicoService servicoService;
    @Autowired
    private PrecoPraticadoRepository precoRepo;
    @Autowired
    private ServicoRepository servicoRepo;
 
    public ServicoController(ServicoService servicoService) { this.servicoService = servicoService; }

    @Operation(summary = "Listar serviços", description = "Suporta paginação e filtros por nome, categoria (grupo) e status")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<Servico>> list(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<Servico> result = servicoService.list(nome, grupo, ativo, page, size, sort);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @Operation(summary = "Obter serviço por ID (com materiais)")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ServicoDetailResponse> get(@PathVariable Long id) {
        return servicoService.findById(id)
                .map(s -> {
                    var atv = s.getAtividade();
                    var materiais = s.getMateriais().stream().map(sm -> 
                        new com.finesse.dto.ServicoMaterialResponse(
                                sm.getId(),
                                sm.getMaterial() != null ? sm.getMaterial().getId() : null,
                                sm.getMaterial() != null ? sm.getMaterial().getProduto() : null,
                                sm.getMaterial() != null ? sm.getMaterial().getCustoUnitario() : java.math.BigDecimal.ZERO,
                                sm.getQuantidadeUsada()
                        )
                    ).toList();
                    var optPreco = precoRepo.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(s.getId());
                    if (optPreco.isEmpty()) {
                        optPreco = precoRepo.findFirstByServico_IdOrderByVigenciaInicioDesc(s.getId());
                    }
                    var precoVigente = optPreco.map(p -> p.getPreco() != null ? p.getPreco().doubleValue() : null).orElse(null);
                    var detalhe = new com.finesse.dto.ServicoDetailResponse(
                            s.getId(),
                            s.getNome(),
                            s.getGrupo(),
                            s.getDuracaoMinutos(),
                            atv != null ? atv.getId() : null,
                            atv != null ? atv.getNome() : null,
                            atv != null ? (atv.getAliquotaTotalPct() != null ? atv.getAliquotaTotalPct().doubleValue() : null) : null,
                            atv != null ? (atv.getIssPct() != null ? atv.getIssPct().doubleValue() : null) : null,
                            s.getMargemLucroCustomPct() != null ? s.getMargemLucroCustomPct().doubleValue() : null,
                            s.isAtivo(),
                            precoVigente,
                            materiais
                    );
                    return ResponseEntity.ok(detalhe);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar preços atuais por serviço")
    @GetMapping("/precos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<PrecoAtualResponse>> listarPrecosAtuais() {
        var servicos = servicoRepo.findAll();
        var lista = new java.util.ArrayList<PrecoAtualResponse>(servicos.size());
        for (var s : servicos) {
            var opt = precoRepo.findFirstByServico_IdAndVigenteTrueOrderByVigenciaInicioDesc(s.getId());
            if (opt.isEmpty()) {
                opt = precoRepo.findFirstByServico_IdOrderByVigenciaInicioDesc(s.getId());
            }
            var preco = opt.map(p -> p.getPreco() != null ? p.getPreco().doubleValue() : null).orElse(null);
            lista.add(new PrecoAtualResponse(s.getId(), preco));
        }
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Criar serviço")
    @ApiResponse(responseCode = "201", description = "Criado", content = @Content(schema = @Schema(implementation = Servico.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Servico> create(@Valid @RequestBody Servico body) {
        Servico created = servicoService.create(body);
        if (body.getPrecoPraticadoInput() != null) {
            servicoService.definirPreco(created.getId(), body.getPrecoPraticadoInput());
        }
        return ResponseEntity.created(URI.create("/api/servicos/" + created.getId())).body(created);
    }

    @Operation(summary = "Atualizar serviço")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Servico> update(@PathVariable Long id, @Valid @RequestBody Servico body) {
        var updated = servicoService.update(id, body);
        if (updated.isPresent() && body.getPrecoPraticadoInput() != null) {
            servicoService.definirPreco(id, body.getPrecoPraticadoInput());
        }
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar status (ativar/inativar)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Servico> toggle(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object ativo = payload.get("ativo");
        if (!(ativo instanceof Boolean)) {
            return ResponseEntity.badRequest().build();
        }
        return servicoService.toggleStatus(id, (Boolean) ativo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover serviço")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = servicoService.delete(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
