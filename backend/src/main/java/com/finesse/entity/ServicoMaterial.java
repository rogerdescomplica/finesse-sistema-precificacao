package com.finesse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servico_materiais",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_servico_material",
                        columnNames = { "servico_id", "material_id" })
        })
public class ServicoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false, foreignKey = @ForeignKey(name = "fk_servico_material_servico"))
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false, foreignKey = @ForeignKey(name = "fk_servico_material_material"))
    private Material material;

    @Column(name = "quantidade_usada", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantidadeUsada;

    public ServicoMaterial() {}

    public ServicoMaterial(Servico servico, Material material, BigDecimal quantidadeUsada) {
        this.servico = servico;
        this.material = material;
        this.quantidadeUsada = quantidadeUsada;
    }

    @Transient
    public BigDecimal getCustoPorAtendimento() {
        if (quantidadeUsada == null) return BigDecimal.ZERO;
        BigDecimal unit = (material != null) ? material.getCustoUnitario() : BigDecimal.ZERO;
        return quantidadeUsada.multiply(unit);
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Servico getServico() {
        return servico;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getQuantidadeUsada() {
        return quantidadeUsada;
    }

    public void setQuantidadeUsada(BigDecimal quantidadeUsada) {
        this.quantidadeUsada = quantidadeUsada;
    }

    
}
