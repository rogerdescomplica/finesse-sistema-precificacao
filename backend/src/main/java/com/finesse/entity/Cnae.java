package com.finesse.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cnae")
@EntityListeners(AuditingEntityListener.class)
public class Cnae {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "iss_rate", precision = 5, scale = 2)
    private BigDecimal issRate;

    @Column(name = "icms_rate", precision = 5, scale = 2)
    private BigDecimal icmsRate;

    @Column(name = "pis_rate", precision = 5, scale = 2)
    private BigDecimal pisRate;

    @Column(name = "cofins_rate", precision = 5, scale = 2)
    private BigDecimal cofinsRate;

    @Column(name = "irpj_rate", precision = 5, scale = 2)
    private BigDecimal irpjRate;

    @Column(name = "csll_rate", precision = 5, scale = 2)
    private BigDecimal csllRate;

    @Column(name = "simples_nacional_rate", precision = 5, scale = 2)
    private BigDecimal simplesNacionalRate;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Cnae() {}

    public Cnae(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    // Builder
    public static class Builder {
        private UUID id;
        private String codigo;
        private String descricao;
        private BigDecimal issRate;
        private BigDecimal icmsRate;
        private BigDecimal pisRate;
        private BigDecimal cofinsRate;
        private BigDecimal irpjRate;
        private BigDecimal csllRate;
        private BigDecimal simplesNacionalRate;
        private Boolean ativo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder issRate(BigDecimal issRate) {
            this.issRate = issRate;
            return this;
        }

        public Builder icmsRate(BigDecimal icmsRate) {
            this.icmsRate = icmsRate;
            return this;
        }

        public Builder pisRate(BigDecimal pisRate) {
            this.pisRate = pisRate;
            return this;
        }

        public Builder cofinsRate(BigDecimal cofinsRate) {
            this.cofinsRate = cofinsRate;
            return this;
        }

        public Builder irpjRate(BigDecimal irpjRate) {
            this.irpjRate = irpjRate;
            return this;
        }

        public Builder csllRate(BigDecimal csllRate) {
            this.csllRate = csllRate;
            return this;
        }

        public Builder simplesNacionalRate(BigDecimal simplesNacionalRate) {
            this.simplesNacionalRate = simplesNacionalRate;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Cnae build() {
            Cnae cnae = new Cnae();
            cnae.id = this.id;
            cnae.codigo = this.codigo;
            cnae.descricao = this.descricao;
            cnae.issRate = this.issRate;
            cnae.icmsRate = this.icmsRate;
            cnae.pisRate = this.pisRate;
            cnae.cofinsRate = this.cofinsRate;
            cnae.irpjRate = this.irpjRate;
            cnae.csllRate = this.csllRate;
            cnae.simplesNacionalRate = this.simplesNacionalRate;
            cnae.ativo = this.ativo != null ? this.ativo : true;
            cnae.createdAt = this.createdAt;
            cnae.updatedAt = this.updatedAt;
            return cnae;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getIssRate() {
        return issRate;
    }

    public void setIssRate(BigDecimal issRate) {
        this.issRate = issRate;
    }

    public BigDecimal getIcmsRate() {
        return icmsRate;
    }

    public void setIcmsRate(BigDecimal icmsRate) {
        this.icmsRate = icmsRate;
    }

    public BigDecimal getPisRate() {
        return pisRate;
    }

    public void setPisRate(BigDecimal pisRate) {
        this.pisRate = pisRate;
    }

    public BigDecimal getCofinsRate() {
        return cofinsRate;
    }

    public void setCofinsRate(BigDecimal cofinsRate) {
        this.cofinsRate = cofinsRate;
    }

    public BigDecimal getIrpjRate() {
        return irpjRate;
    }

    public void setIrpjRate(BigDecimal irpjRate) {
        this.irpjRate = irpjRate;
    }

    public BigDecimal getCsllRate() {
        return csllRate;
    }

    public void setCsllRate(BigDecimal csllRate) {
        this.csllRate = csllRate;
    }

    public BigDecimal getSimplesNacionalRate() {
        return simplesNacionalRate;
    }

    public void setSimplesNacionalRate(BigDecimal simplesNacionalRate) {
        this.simplesNacionalRate = simplesNacionalRate;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo != null ? ativo : true;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Método auxiliar para obter a alíquota total (soma de todas as taxas)
    public BigDecimal getAliquota() {
        BigDecimal aliquota = BigDecimal.ZERO;
        if (issRate != null) aliquota = aliquota.add(issRate);
        if (icmsRate != null) aliquota = aliquota.add(icmsRate);
        if (pisRate != null) aliquota = aliquota.add(pisRate);
        if (cofinsRate != null) aliquota = aliquota.add(cofinsRate);
        if (irpjRate != null) aliquota = aliquota.add(irpjRate);
        if (csllRate != null) aliquota = aliquota.add(csllRate);
        if (simplesNacionalRate != null) aliquota = aliquota.add(simplesNacionalRate);
        return aliquota;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cnae cnae = (Cnae) o;
        return Objects.equals(id, cnae.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Cnae{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}