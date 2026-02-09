package com.finesse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrecoSetRequest(
        BigDecimal preco,
        LocalDate vigenciaInicio
) {}
