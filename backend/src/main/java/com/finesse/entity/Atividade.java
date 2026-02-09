package com.finesse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** 
 * Entidade que representa uma Atividade (CNAE/Alíquotas)
 */
@Entity
@Table(name = "atividades")      
public class Atividade {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 20)
    private String cnae;

    // Ex.: 16.72 (percentual)
    @Column(name = "aliquota_total_pct", nullable = false, precision = 7, scale = 4)
    private BigDecimal aliquotaTotalPct;

    // Ex.: 2.84 (percentual)
    @Column(name = "iss_pct", nullable = false, precision = 7, scale = 4)
    private BigDecimal issPct;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private boolean ativo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "atividade", fetch = FetchType.LAZY)
    private List<Servico> servicos = new ArrayList<>();

    public Atividade() {}

    @Transient
    public BigDecimal getAliquotaTotalFrac() {
        return pctToFrac(aliquotaTotalPct);
    }

    @Transient
    public BigDecimal getIssFrac() {
        return pctToFrac(issPct);
    }

    private BigDecimal pctToFrac(BigDecimal pct) {
        if (pct == null) return BigDecimal.ZERO;
        return pct.divide(new BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
    }

    public BigDecimal getAliquotaTotalPct() {
        return aliquotaTotalPct;
    }

    public void setAliquotaTotalPct(BigDecimal aliquotaTotalPct) {
        this.aliquotaTotalPct = aliquotaTotalPct;
    }

    public BigDecimal getIssPct() {
        return issPct;
    }

    public void setIssPct(BigDecimal issPct) {
        this.issPct = issPct;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    
}
