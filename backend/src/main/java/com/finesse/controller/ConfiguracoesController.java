package com.finesse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.finesse.dto.PageResponse;
import com.finesse.entity.Configuracoes;
import com.finesse.service.ConfiguracoesService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Configurações", description = "CRUD de Configurações Gerais")
@RequestMapping({"/api/config", "/api/configuracoes"})
public class ConfiguracoesController {

    @Autowired
    private ConfiguracoesService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<Configuracoes>> list(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<Configuracoes> result = service.list(ativo, page, size, sort);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Configuracoes> get(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Configuracoes> create(@RequestBody Configuracoes body) {
        Configuracoes created = service.create(body);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Configuracoes> update(@PathVariable Long id, @RequestBody Configuracoes body) {
        return service.update(id, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Configuracoes> toggle(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {
        Object ativo = payload.get("ativo");
        if (!(ativo instanceof Boolean)) {
            return ResponseEntity.badRequest().build();
        }
        return service.toggleStatus(id, (Boolean) ativo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = service.delete(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
