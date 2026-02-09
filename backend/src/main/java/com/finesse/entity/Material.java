package com.finesse.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa os materiais/insumos utilizados nos serviços
 */
@Entity
@Table(name = "materiais")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Column(nullable = false, length = 200)
    private String produto;

    @NotNull(message = "Unidade de medida é obrigatória")
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private UnidadeMedida unidadeMedida;

    @NotNull(message = "Volume da embalagem é obrigatório")
    @Positive(message = "Volume deve ser positivo")
    @Column(name = "volume_embalagem", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeEmbalagem;

    @NotNull(message = "Preço da embalagem é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    @Column(name = "preco_embalagem", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoEmbalagem;

    @Column(name = "custo_unitario", nullable = false, precision = 10, scale = 6, insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal custoUnitario;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Material() {
    }

    public Material(String produto, UnidadeMedida unidadeMedida, BigDecimal volumeEmbalagem, 
                   BigDecimal precoEmbalagem) {
        this.produto = produto;
        this.unidadeMedida = unidadeMedida;
        this.volumeEmbalagem = volumeEmbalagem;
        this.precoEmbalagem = precoEmbalagem;
    }

    @PrePersist
    protected void onCreate() {
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // custo_unitario é coluna gerada pelo banco (stored), não calcular nem persistir via JPA

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getVolumeEmbalagem() {
        return volumeEmbalagem;
    }

    public void setVolumeEmbalagem(BigDecimal volumeEmbalagem) {
        this.volumeEmbalagem = volumeEmbalagem;
    }

    public BigDecimal getPrecoEmbalagem() {
        return precoEmbalagem;
    }

    public void setPrecoEmbalagem(BigDecimal precoEmbalagem) {
        this.precoEmbalagem = precoEmbalagem;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(id, material.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", produto='" + produto + '\'' +
                ", unidadeMedida=" + unidadeMedida +
                ", custoUnitario=" + custoUnitario +
                '}';
    }
}
