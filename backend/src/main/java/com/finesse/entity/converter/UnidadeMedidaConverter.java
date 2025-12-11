package com.finesse.entity.converter;

import com.finesse.entity.UnidadeMedida;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UnidadeMedidaConverter implements AttributeConverter<UnidadeMedida, String> {
    @Override
    public String convertToDatabaseColumn(UnidadeMedida attribute) {
        if (attribute == null) return null;
        return attribute.getSigla();
    }

    @Override
    public UnidadeMedida convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim().toLowerCase();
        switch (v) {
            case "un": return UnidadeMedida.UNIDADE;
            case "m": return UnidadeMedida.METRO;
            case "ml": return UnidadeMedida.MILILITRO;
            case "g": return UnidadeMedida.GRAMA;
            case "l": return UnidadeMedida.LITRO;
            case "kg": return UnidadeMedida.QUILOGRAMA;
            default: throw new IllegalArgumentException("UnidadeMedida inválida: " + dbData);
        }
    }
}

