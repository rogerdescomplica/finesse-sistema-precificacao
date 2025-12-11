package com.finesse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade que representa os serviços prestados
 */
@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do serviço é obrigatório")
    @Column(nullable = false, length = 200)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @NotNull(message = "Duração é obrigatória")
    @Column(nullable = false)
    private Duration duracao;

    @NotNull(message = "Valor hora é obrigatório")
    @Positive(message = "Valor hora deve ser positivo")
    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "custo_materiais", precision = 10, scale = 2)
    private BigDecimal custoMateriais = BigDecimal.ZERO;

    @Column(name = "custo_total", precision = 10, scale = 2)
    private BigDecimal custoTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aliquota_cnae_id")
    private AliquotaCNAE aliquotaCNAE;

    @Column(name = "percentual_imposto", precision = 5, scale = 2)
    private BigDecimal percentualImposto;

    @Column(name = "percentual_custo_fixo", precision = 5, scale = 2)
    private BigDecimal percentualCustoFixo;

    @Column(name = "percentual_margem_lucro", precision = 5, scale = 2)
    private BigDecimal percentualMargemLucro;

    @NotNull(message = "Markup é obrigatório")
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal markup = BigDecimal.ONE;

    @Column(name = "valor_venda_sugerido", precision = 10, scale = 2)
    private BigDecimal valorVendaSugerido;

    @Column(name = "valor_venda_atual", precision = 10, scale = 2)
    private BigDecimal valorVendaAtual;

    @Column(name = "lucro_liquido_rs", precision = 10, scale = 2)
    private BigDecimal lucroLiquidoRS;

    @Column(name = "lucro_liquido_percentual", precision = 5, scale = 2)
    private BigDecimal lucroLiquidoPercentual;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServicoMaterial> materiais = new HashSet<>();

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Servico() {
    }

    public Servico(String nome, Duration duracao, BigDecimal valorHora) {
        this.nome = nome;
        this.duracao = duracao;
        this.valorHora = valorHora;
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

    /**
     * Adiciona um material ao serviço
     */
    public void adicionarMaterial(ServicoMaterial servicoMaterial) {
        materiais.add(servicoMaterial);
        servicoMaterial.setServico(this);
    }

    /**
     * Remove um material do serviço
     */
    public void removerMaterial(ServicoMaterial servicoMaterial) {
        materiais.remove(servicoMaterial);
        servicoMaterial.setServico(null);
    }

    /**
     * Calcula o custo total dos materiais
     */
    public void calcularCustoMateriais() {
        this.custoMateriais = materiais.stream()
            .map(ServicoMaterial::getCustoTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o custo total do serviço (valor hora + materiais)
     */
    public void calcularCustoTotal() {
        BigDecimal custoHora = calcularCustoHora();
        this.custoTotal = custoHora.add(custoMateriais != null ? custoMateriais : BigDecimal.ZERO);
    }

    /**
     * Calcula o custo da hora baseado no tempo de duração
     */
    private BigDecimal calcularCustoHora() {
        if (valorHora == null || duracao == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal horas = BigDecimal.valueOf(duracao.toMinutes())
            .divide(BigDecimal.valueOf(60), 4, java.math.RoundingMode.HALF_UP);
        
        return valorHora.multiply(horas);
    }

    /**
     * Calcula o markup baseado nos percentuais de imposto, custo fixo e margem de lucro
     */
    public void calcularMarkup() {
        BigDecimal umMenos = BigDecimal.ONE;
        
        BigDecimal somaPercentuais = BigDecimal.ZERO;
        if (percentualImposto != null) {
            somaPercentuais = somaPercentuais.add(percentualImposto.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
        }
        if (percentualCustoFixo != null) {
            somaPercentuais = somaPercentuais.add(percentualCustoFixo.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
        }
        if (percentualMargemLucro != null) {
            somaPercentuais = somaPercentuais.add(percentualMargemLucro.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
        }
        
        BigDecimal divisor = umMenos.subtract(somaPercentuais);
        if (divisor.compareTo(BigDecimal.ZERO) > 0) {
            this.markup = umMenos.divide(divisor, 4, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula o valor de venda sugerido
     */
    public void calcularValorVendaSugerido() {
        if (custoTotal != null && markup != null) {
            this.valorVendaSugerido = custoTotal.multiply(markup)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula o lucro líquido em R$ e percentual
     */
    public void calcularLucroLiquido() {
        if (valorVendaAtual != null && custoTotal != null) {
            this.lucroLiquidoRS = valorVendaAtual.subtract(custoTotal);
            
            if (valorVendaAtual.compareTo(BigDecimal.ZERO) > 0) {
                this.lucroLiquidoPercentual = lucroLiquidoRS
                    .divide(valorVendaAtual, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }
    }

    /**
     * Recalcula todos os valores do serviço
     */
    public void recalcular() {
        calcularCustoMateriais();
        calcularCustoTotal();
        calcularMarkup();
        calcularValorVendaSugerido();
        if (valorVendaAtual != null) {
            calcularLucroLiquido();
        }
    }

    // Getters and Setters
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

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Duration getDuracao() {
        return duracao;
    }

    public void setDuracao(Duration duracao) {
        this.duracao = duracao;
    }

    public BigDecimal getValorHora() {
        return valorHora;
    }

    public void setValorHora(BigDecimal valorHora) {
        this.valorHora = valorHora;
    }

    public BigDecimal getCustoMateriais() {
        return custoMateriais;
    }

    public void setCustoMateriais(BigDecimal custoMateriais) {
        this.custoMateriais = custoMateriais;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public AliquotaCNAE getAliquotaCNAE() {
        return aliquotaCNAE;
    }

    public void setAliquotaCNAE(AliquotaCNAE aliquotaCNAE) {
        this.aliquotaCNAE = aliquotaCNAE;
    }

    public BigDecimal getPercentualImposto() {
        return percentualImposto;
    }

    public void setPercentualImposto(BigDecimal percentualImposto) {
        this.percentualImposto = percentualImposto;
    }

    public BigDecimal getPercentualCustoFixo() {
        return percentualCustoFixo;
    }

    public void setPercentualCustoFixo(BigDecimal percentualCustoFixo) {
        this.percentualCustoFixo = percentualCustoFixo;
    }

    public BigDecimal getPercentualMargemLucro() {
        return percentualMargemLucro;
    }

    public void setPercentualMargemLucro(BigDecimal percentualMargemLucro) {
        this.percentualMargemLucro = percentualMargemLucro;
    }

    public BigDecimal getMarkup() {
        return markup;
    }

    public void setMarkup(BigDecimal markup) {
        this.markup = markup;
    }

    public BigDecimal getValorVendaSugerido() {
        return valorVendaSugerido;
    }

    public void setValorVendaSugerido(BigDecimal valorVendaSugerido) {
        this.valorVendaSugerido = valorVendaSugerido;
    }

    public BigDecimal getValorVendaAtual() {
        return valorVendaAtual;
    }

    public void setValorVendaAtual(BigDecimal valorVendaAtual) {
        this.valorVendaAtual = valorVendaAtual;
    }

    public BigDecimal getLucroLiquidoRS() {
        return lucroLiquidoRS;
    }

    public void setLucroLiquidoRS(BigDecimal lucroLiquidoRS) {
        this.lucroLiquidoRS = lucroLiquidoRS;
    }

    public BigDecimal getLucroLiquidoPercentual() {
        return lucroLiquidoPercentual;
    }

    public void setLucroLiquidoPercentual(BigDecimal lucroLiquidoPercentual) {
        this.lucroLiquidoPercentual = lucroLiquidoPercentual;
    }

    public Set<ServicoMaterial> getMateriais() {
        return materiais;
    }

    public void setMateriais(Set<ServicoMaterial> materiais) {
        this.materiais = materiais;
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
        Servico servico = (Servico) o;
        return Objects.equals(id, servico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Servico{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", duracao=" + duracao +
                ", valorVendaAtual=" + valorVendaAtual +
                '}';
    }
}