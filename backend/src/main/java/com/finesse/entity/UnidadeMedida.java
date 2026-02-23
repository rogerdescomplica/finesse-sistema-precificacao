package com.finesse.entity;

/**
 * Enumeração das unidades de medida utilizadas no sistema
 */
public enum UnidadeMedida {
    UN("UN", "Unidade"),
    M("M", "Metro"),
    ML("ML", "Mililitro"),
    G("G", "Grama"),
    L("L", "Litro"),
    KG("KG", "Quilograma"),
    UI("UI", "Unidade de Internacional");

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
        String v = value.trim().toUpperCase();
        switch (v) {
            case "UN": case "UNIDADE": return UN;
            case "M": case "METRO": return M;
            case "ML": case "MILILITRO": return ML;
            case "G": case "GRAMA": return G;
            case "L": case "LITRO": return L;
            case "KG": case "QUILOGRAMA": return KG;
            default: throw new IllegalArgumentException("UnidadeMedida inválida: " + value);
        }
    }
}
