package com.finesse.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.finesse.entity.Material;

/**
 * Camada de serviço para Materiais.
 * Responsável por regras de negócio, validações e integração com persistência.
 */
public interface MaterialService {

    /**
     * Lista materiais com filtros e paginação.
     * @param produto Filtro por nome do produto (parcial, case-insensitive)
     * @param ativo Filtro por status ativo/inativo; null para todos
     * @param page Página (0-based)
     * @param size Tamanho da página
     * @param sort Ordenação no formato "campo,asc|desc"
     * @return Página com materiais
     */
    Page<Material> list(String produto, Boolean ativo, int page, int size, String sort);

    /**
     * Busca material por ID.
     * @param id Identificador do material
     * @return Optional contendo material, se existir
     */
    Optional<Material> findById(Long id);

    /**
     * Cria novo material.
     * @param m Entidade Material a criar
     * @return Material persistido
     * @throws com.finesse.exception.ValidationException em caso de dados inválidos
     * @throws com.finesse.exception.ServiceOperationException em falhas de operação
     */
    Material create(Material m);

    /**
     * Atualiza material existente.
     * @param id ID do material
     * @param input Dados para atualização
     * @return Optional com material atualizado
     * @throws com.finesse.exception.ValidationException em caso de dados inválidos
     * @throws com.finesse.exception.ServiceOperationException em falhas de operação
     */
    Optional<Material> update(Long id, Material input);

    /**
     * Exclui material pelo ID.
     * @param id ID do material
     * @return true se removido; false se não encontrado
     * @throws com.finesse.exception.ServiceOperationException em falhas de operação
     */
    boolean delete(Long id);

    /**
     * Alterna status ativo/inativo.
     * @param id ID do material
     * @param ativo Novo status
     * @return Optional com material atualizado
     * @throws com.finesse.exception.ServiceOperationException em falhas de operação
     */
    Optional<Material> toggleStatus(Long id, boolean ativo);
}
