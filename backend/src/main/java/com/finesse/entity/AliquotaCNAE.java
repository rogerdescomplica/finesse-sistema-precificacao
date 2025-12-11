package com.finesse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa as alíquotas de impostos por atividade (CNAE)
 */
@Entity
@Table(name = "aliquotas_cnae")
public class AliquotaCNAE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da atividade é obrigatório")
    @Column(nullable = false, length = 100)
    private String atividade;

    @NotBlank(message = "CNAE é obrigatório")
    @Column(nullable = false, unique = true, length = 20)
    private String cnae;

    @NotNull(message = "Alíquota total é obrigatória")
    @Positive(message = "Alíquota total deve ser positiva")
    @Column(name = "aliquota_total", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaTotal;

    @NotNull(message = "ISS é obrigatório")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal iss;

    @Column(length = 200)
    private String observacoes;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public AliquotaCNAE() {
    }

    public AliquotaCNAE(String atividade, String cnae, BigDecimal aliquotaTotal, BigDecimal iss) {
        this.atividade = atividade;
        this.cnae = cnae;
        this.aliquotaTotal = aliquotaTotal;
        this.iss = iss;
    }

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAtividade() {
        return atividade;
    }

    public void setAtividade(String atividade) {
        this.atividade = atividade;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
    }

    public BigDecimal getAliquotaTotal() {
        return aliquotaTotal;
    }

    public void setAliquotaTotal(BigDecimal aliquotaTotal) {
        this.aliquotaTotal = aliquotaTotal;
    }

    public BigDecimal getIss() {
        return iss;
    }

    public void setIss(BigDecimal iss) {
        this.iss = iss;
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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
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
        AliquotaCNAE that = (AliquotaCNAE) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AliquotaCNAE{" +
                "id=" + id +
                ", atividade='" + atividade + '\'' +
                ", cnae='" + cnae + '\'' +
                ", aliquotaTotal=" + aliquotaTotal +
                '}';
    }
}