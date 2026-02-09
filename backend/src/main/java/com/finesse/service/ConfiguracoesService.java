package com.finesse.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.finesse.entity.Configuracoes;

/**
 * Camada de serviço para Configurações.
 * Responsável por regras e persistência de parâmetros do sistema.
 */
public interface ConfiguracoesService {

    /**
     * Lista configurações com paginação e filtro de status.
     * @param ativo filtro ativo/inativo; null para todos
     * @param page página (0-based)
     * @param size tamanho da página
     * @param sort campo de ordenação
     * @return página de configurações
     */
    Page<Configuracoes> list(Boolean ativo, int page, int size, String sort);

    /**
     * Busca por ID.
     * @param id identificador
     * @return Optional com configuração
     */
    Optional<Configuracoes> findById(Long id);

    /**
     * Cria nova configuração.
     * @param c entidade
     * @return configuração criada
     */
    Configuracoes create(Configuracoes c);

    /**
     * Atualiza configuração existente.
     * @param id ID
     * @param input dados
     * @return Optional com configuração atualizada
     */
    Optional<Configuracoes> update(Long id, Configuracoes input);

    /**
     * Remove por ID.
     * @param id ID
     * @return true se removida
     */
    boolean delete(Long id);

    /**
     * Alterna status.
     * @param id ID
     * @param ativo novo status
     * @return Optional com configuração atualizada
     */
    Optional<Configuracoes> toggleStatus(Long id, boolean ativo);
}
