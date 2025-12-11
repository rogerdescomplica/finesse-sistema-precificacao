package com.finesse.entity;

/**
 * Enumeração das unidades de medida utilizadas no sistema
 */
public enum UnidadeMedida {
    UNIDADE("un", "Unidade"),
    METRO("m", "Metro"),
    MILILITRO("ml", "Mililitro"),
    GRAMA("g", "Grama"),
    LITRO("l", "Litro"),
    QUILOGRAMA("kg", "Quilograma");

    private final String sigla;
    private final String descricao;

    UnidadeMedida(String sigla, String descricao) {
        this.sigla = sigla;
        this.descricao = descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public static UnidadeMedida fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase();
        switch (v) {
            case "un": case "unidade": return UNIDADE;
            case "m": case "metro": return METRO;
            case "ml": case "mililitro": return MILILITRO;
            case "g": case "grama": return GRAMA;
            case "l": case "litro": return LITRO;
            case "kg": case "quilograma": return QUILOGRAMA;
            default: throw new IllegalArgumentException("UnidadeMedida inválida: " + value);
        }
    }
}
