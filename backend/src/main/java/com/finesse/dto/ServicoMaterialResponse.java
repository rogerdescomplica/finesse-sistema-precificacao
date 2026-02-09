package com.finesse.dto;

import java.math.BigDecimal;

public record ServicoMaterialResponse(
        Long id,
        Long materialId,
        String produto,
        BigDecimal custoUnitario,
        BigDecimal quantidadeUsada
) {}
