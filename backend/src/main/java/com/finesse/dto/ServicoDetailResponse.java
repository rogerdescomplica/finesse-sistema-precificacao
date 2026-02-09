package com.finesse.dto;

import java.util.List;

public record ServicoDetailResponse(
        Long id,
        String nome,
        String grupo,
        Integer duracaoMinutos,
        Long atividadeId,
        String atividadeNome,
        Double atividadeAliquotaTotalPct,
        Double atividadeIssPct,
        Double margemLucroCustomPct,
        boolean ativo,
        Double precoVigente,
        List<ServicoMaterialResponse> materiais
) {}
