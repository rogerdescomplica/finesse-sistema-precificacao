package com.finesse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa o relacionamento entre Serviço e Material
 * com informações de quantidade utilizada
 */
@Entity
@Table(name = "servico_materiais")
public class ServicoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @NotNull(message = "Quantidade usada é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    @Column(name = "quantidade_usada", nullable = false, precision = 10, scale = 4)
    private BigDecimal quantidadeUsada;

    @Column(name = "custo_total", precision = 10, scale = 6)
    private BigDecimal custoTotal;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public ServicoMaterial() {
    }

    public ServicoMaterial(Servico servico, Material material, BigDecimal quantidadeUsada) {
        this.servico = servico;
        this.material = material;
        this.quantidadeUsada = quantidadeUsada;
    }

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        calcularCustoTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
        calcularCustoTotal();
    }

    /**
     * Calcula o custo total baseado na quantidade usada e no custo unitário do material
     */
    private void calcularCustoTotal() {
        if (material != null && material.getCustoUnitario() != null && quantidadeUsada != null) {
            this.custoTotal = material.getCustoUnitario().multiply(quantidadeUsada);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
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

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
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
        ServicoMaterial that = (ServicoMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ServicoMaterial{" +
                "id=" + id +
                ", quantidadeUsada=" + quantidadeUsada +
                ", custoTotal=" + custoTotal +
                '}';
    }
}