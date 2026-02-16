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

import com.finesse.entity.Atividade;
import com.finesse.dto.PageResponse;
import com.finesse.service.AtividadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Atividades", description = "CRUD de Atividades")
@RequestMapping("/api/atividades")
@Validated
public class AtividadeController {

    @Autowired
    private AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) { this.atividadeService = atividadeService; }

    @Operation(summary = "Listar atividades", description = "Suporta paginação e filtros por título e status")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<Atividade>> list(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<Atividade> result = atividadeService.list(titulo, ativo, page, size, sort);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @Operation(summary = "Obter atividade por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Atividade> get(@PathVariable Long id) {
        return atividadeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar atividade")
    @ApiResponse(responseCode = "201", description = "Criado", content = @Content(schema = @Schema(implementation = Atividade.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Atividade> create(@Valid @RequestBody Atividade body) {
        Atividade created = atividadeService.create(body);
        return ResponseEntity.created(URI.create("/api/atividades/" + created.getId())).body(created);
    }

    @Operation(summary = "Atualizar atividade")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Atividade> update(@PathVariable Long id, @Valid @RequestBody Atividade body) {
        return atividadeService.update(id, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar status (ativar/inativar)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Atividade> toggle(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object ativo = payload.get("ativo");
        if (!(ativo instanceof Boolean)) {
            return ResponseEntity.badRequest().build();
        }
        return atividadeService.toggleStatus(id, (Boolean) ativo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover atividade")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = atividadeService.delete(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
