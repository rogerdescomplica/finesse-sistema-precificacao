package com.finesse.dto;

import com.finesse.entity.PrecoPraticado;

public record PrecoSetResponse(
        boolean changed,
        PrecoPraticado vigente
) {}
