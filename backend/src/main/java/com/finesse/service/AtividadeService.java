package com.finesse.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.finesse.entity.Atividade;

/**
 * Camada de serviço para Atividades.
 * Reúne regras de negócio e integração com persistência.
 */
public interface AtividadeService {

    /**
     * Lista atividades com filtros e paginação.
     * @param titulo Filtro por título (parcial, acento-insensível)
     * @param ativo Filtro por ativo/inativo; null para todos
     * @param page Página (0-based)
     * @param size Tamanho da página
     * @param sort Ordenação "campo,asc|desc"
     * @return Página de atividades
     */
    Page<Atividade> list(String titulo, Boolean ativo, int page, int size, String sort);

    /**
     * Busca atividade por ID.
     * @param id Identificador
     * @return Optional com atividade
     */
    Optional<Atividade> findById(Long id);

    /**
     * Cria atividade.
     * @param a entidade a persistir
     * @return atividade criada
     */
    Atividade create(Atividade a);

    /**
     * Atualiza atividade existente.
     * @param id ID
     * @param input dados novos
     * @return Optional com atividade atualizada
     */
    Optional<Atividade> update(Long id, Atividade input);

    /**
     * Remove atividade por ID.
     * @param id ID
     * @return true se removida
     */
    boolean delete(Long id);

    /**
     * Alterna status ativo/inativo.
     * @param id ID
     * @param ativo novo status
     * @return Optional com atividade atualizada
     */
    Optional<Atividade> toggleStatus(Long id, boolean ativo);
}
