package com.finesse.dto.auth;

import java.util.List;

public record LoginResponseRecord(
    String token,
    String type,
    Long id,
    String nome,
    String email,
    List<String> perfis
) { }
