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

import com.finesse.entity.Material;
import com.finesse.dto.PageResponse;
import com.finesse.service.MaterialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Materiais", description = "CRUD de Materiais")
@RequestMapping({"/api/material", "/api/materiais"})
@Validated
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    public MaterialController(MaterialService materialService) { this.materialService = materialService; }

    @Operation(summary = "Listar materiais", description = "Suporta paginação e filtros por produto e status")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<Material>> list(
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<Material> result = materialService.list(produto, ativo, page, size, sort);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @Operation(summary = "Obter material por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Material> get(@PathVariable Long id) {
        return materialService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar material")
    @ApiResponse(responseCode = "201", description = "Criado", content = @Content(schema = @Schema(implementation = Material.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Material> create(@Valid @RequestBody Material body) {
        Material created = materialService.create(body);
        return ResponseEntity.created(URI.create("/api/materiais/" + created.getId())).body(created);
    }

    @Operation(summary = "Atualizar material")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Material> update(@PathVariable Long id, @Valid @RequestBody Material body) {
        return materialService.update(id, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Alterar status (ativar/inativar)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Material> toggle(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object ativo = payload.get("ativo");
        if (!(ativo instanceof Boolean)) {
            return ResponseEntity.badRequest().build();
        }
        return materialService.toggleStatus(id, (Boolean) ativo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover material")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = materialService.delete(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
