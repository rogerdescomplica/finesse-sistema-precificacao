package com.finesse.entity.converter;

import com.finesse.entity.UnidadeMedida;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UnidadeMedidaConverter implements AttributeConverter<UnidadeMedida, String> {
    @Override
    public String convertToDatabaseColumn(UnidadeMedida attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public UnidadeMedida convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String normalized = dbData.trim().toUpperCase();
        return UnidadeMedida.valueOf(normalized);
    }
}
