package com.finesse.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finesse.dto.AtualizarUsuarioRequestRecord;
import com.finesse.dto.CriarUsuarioRequestRecord;
import com.finesse.dto.UsuarioRecord;
import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
import com.finesse.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/**
 * Controller para gerenciamento de usuários
 * Apenas usuários com perfil ADMIN podem acessar
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Listar todos os usuários
     * GET /api/usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRecord>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioRecord> dtos = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Listar apenas usuários ativos
     * GET /api/usuarios/ativos
     */
    @GetMapping("/ativos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRecord>> listarAtivos() {
        List<Usuario> usuarios = usuarioService.listarAtivos();
        List<UsuarioRecord> dtos = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar usuário por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            UsuarioRecord dto = convertToDTO(usuario);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Buscar usuário por email
     * GET /api/usuarios/email/{email}
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarPorEmail(@PathVariable String email) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(email);
            UsuarioRecord dto = convertToDTO(usuario);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Criar novo usuário
     * POST /api/usuarios
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(@Valid @RequestBody CriarUsuarioRequestRecord request) {
        try {
            Usuario usuario = usuarioService.criarUsuario(
                    request.nome(),
                    request.email(),
                    request.senha(),
                    request.perfil()
            );
            UsuarioRecord dto = convertToDTO(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Atualizar usuário existente
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar( @PathVariable Long id, @Valid @RequestBody AtualizarUsuarioRequestRecord request) {
        try {
            Usuario usuario = this.usuarioService.atualizarUsuario(id, request);
            UsuarioRecord dto = convertToDTO(usuario);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Alterar senha de um usuário
     * PUT /api/usuarios/{id}/senha
     */
    @PutMapping("/{id}/senha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> alterarSenha(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        try {
            String novaSenha = request.get("novaSenha");
            if (novaSenha == null || novaSenha.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Nova senha é obrigatória"));
            }

            usuarioService.alterarSenha(id, novaSenha);
            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

     @Operation(summary = "Alterar status (ativar/inativar)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<UsuarioRecord> toggle(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object ativo = payload.get("ativo");
        if (!(ativo instanceof Boolean)) {
            return ResponseEntity.badRequest().build();
        }
        return usuarioService.toggleStatus(id, (Boolean) ativo)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletar usuário (exclusão física - use com cuidado!)
     * DELETE /api/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            // Não permitir que o admin delete a si mesmo
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            
            if (usuarioLogado.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Você não pode deletar sua própria conta"));
            }

            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok(Map.of("message", "Usuário deletado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Buscar usuários por nome (busca parcial)
     * GET /api/usuarios/buscar?nome={nome}
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRecord>> buscarPorNome(@RequestParam String nome) {
        List<Usuario> usuarios = usuarioService.buscarPorNome(nome);
        List<UsuarioRecord> dtos = usuarios.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Estatísticas de usuários
     * GET /api/usuarios/estatisticas
     */
    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> estatisticas = new HashMap<>();
        
        List<Usuario> todos = usuarioService.listarTodos();
        List<Usuario> ativos = usuarioService.listarAtivos();
        
        long totalAdmins = todos.stream()
                .filter(u -> u.temPerfil(Perfil.ADMIN))
                .count();
        
        long totalVisualizadores = todos.stream()
                .filter(u -> u.temPerfil(Perfil.VISUALIZADOR))
                .count();
        
        estatisticas.put("total", todos.size());
        estatisticas.put("ativos", ativos.size());
        estatisticas.put("inativos", todos.size() - ativos.size());
        estatisticas.put("admins", totalAdmins);
        estatisticas.put("visualizadores", totalVisualizadores);
        
        return ResponseEntity.ok(estatisticas);
    }

    // Método auxiliar para converter Usuario para DTO
    private UsuarioRecord convertToDTO(Usuario usuario) {
        String perfil = usuario.isAdmin() ? Perfil.ADMIN.name() : Perfil.VISUALIZADOR.name();
        return new UsuarioRecord(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getAtivo(),
            perfil,
            usuario.isAdmin()
        );
    }
}
