package com.finesse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;

@Entity
@Table(name = "configuracoes")
public class Configuracoes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ====== Financeiro ====== */

    @Column(name = "pretensao_salarial_mensal", nullable = false, precision = 14, scale = 2)
    private BigDecimal pretensaoSalarialMensal;

    @Column(name = "horas_semanais", nullable = false, precision = 10, scale = 2)
    private BigDecimal horasSemanais;

    @Column(name = "semanas_media_mes", nullable = false, precision = 10, scale = 4)
    private BigDecimal semanasMediaMes = new BigDecimal("4.33");

    /* ====== Precificação ====== */

    // Ex.: 10.62
    @Column(name = "custo_fixo_pct", nullable = false, precision = 7, scale = 4)
    private BigDecimal custoFixoPct;

    // Ex.: 20.00
    @Column(name = "margem_lucro_padrao_pct", nullable = false, precision = 7, scale = 4)
    private BigDecimal margemLucroPadraoPct;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    @Column(nullable = false)
    private boolean ativo = true;

    public Configuracoes() {}

    /* ====== Derivados (não persistir) ====== */

    @Transient
    public BigDecimal getHorasMensais() {
        if (horasSemanais == null) return BigDecimal.ZERO;
        return horasSemanais.multiply(semanasMediaMes);
    }

    @Transient
    public BigDecimal getValorHora() {
        if (pretensaoSalarialMensal == null) return BigDecimal.ZERO;
        BigDecimal horasMes = getHorasMensais();
        if (horasMes.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return pretensaoSalarialMensal.divide(horasMes, 8, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getValorMinuto() { // Valor por minuto
        return getValorHora().divide(new BigDecimal("60"), 8, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getCustoFixoFrac() { // Custo fixo como fração do valor por minuto
        return pctToFrac(custoFixoPct);
    }

    @Transient
    public BigDecimal getMargemPadraoFrac() {
        return pctToFrac(margemLucroPadraoPct);
    }

    private BigDecimal pctToFrac(BigDecimal pct) { // Converter porcentagem para fração
        if (pct == null) return BigDecimal.ZERO;
        return pct.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPretensaoSalarialMensal() {
        return pretensaoSalarialMensal;
    }

    public void setPretensaoSalarialMensal(BigDecimal pretensaoSalarialMensal) {
        this.pretensaoSalarialMensal = pretensaoSalarialMensal;
    }

    public BigDecimal getHorasSemanais() {
        return horasSemanais;
    }

    public void setHorasSemanais(BigDecimal horasSemanais) {
        this.horasSemanais = horasSemanais;
    }

    public BigDecimal getSemanasMediaMes() {
        return semanasMediaMes;
    }

    public void setSemanasMediaMes(BigDecimal semanasMediaMes) {
        this.semanasMediaMes = semanasMediaMes;
    }

    public BigDecimal getCustoFixoPct() {
        return custoFixoPct;
    }

    public void setCustoFixoPct(BigDecimal custoFixoPct) {
        this.custoFixoPct = custoFixoPct;
    }

    public BigDecimal getMargemLucroPadraoPct() {
        return margemLucroPadraoPct;
    }

    public void setMargemLucroPadraoPct(BigDecimal margemLucroPadraoPct) {
        this.margemLucroPadraoPct = margemLucroPadraoPct;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    
}
