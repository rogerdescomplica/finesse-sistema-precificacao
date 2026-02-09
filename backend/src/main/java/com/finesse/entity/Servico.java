package com.finesse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nome;

    @Column(nullable = false, length = 80)
    private String grupo;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "atividade_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_servico_atividade"))
    private Atividade atividade;

    /**
     * margem de lucro personalizada por serviço (percentual "humano")
     * Se null, usa Configuracao.margemLucroPadraoPct
     */
    @Column(name = "margem_lucro_custom_pct", precision = 7, scale = 4)
    private BigDecimal margemLucroCustomPct;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<ServicoMaterial> materiais = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OrderBy("vigenciaInicio DESC")
    private List<PrecoPraticado> precosPraticados = new ArrayList<>();

    public Servico() {}

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private BigDecimal precoPraticadoInput;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDate precoVigenciaInicioInput;

    @Transient
    public boolean isUsaMargemCustom() {
        return margemLucroCustomPct != null;
    }

    @Transient
    public BigDecimal getMargemCustomFrac() {
        if (margemLucroCustomPct == null) return null;
        return margemLucroCustomPct.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    }

    // Helpers bidirecionais
    public void addMaterial(Material material, BigDecimal quantidadeUsada) {
        ServicoMaterial sm = new ServicoMaterial(this, material, quantidadeUsada);
        materiais.add(sm);
    }

    public void removeMaterial(ServicoMaterial sm) {
        materiais.remove(sm);
        sm.setServico(null);
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

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = atividade;
    }

    public BigDecimal getMargemLucroCustomPct() {
        return margemLucroCustomPct;
    }

    public void setMargemLucroCustomPct(BigDecimal margemLucroCustomPct) {
        this.margemLucroCustomPct = margemLucroCustomPct;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<ServicoMaterial> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<ServicoMaterial> materiais) {
        this.materiais = materiais;
    }

    public List<PrecoPraticado> getPrecosPraticados() {
        return precosPraticados;
    }

    public void setPrecosPraticados(List<PrecoPraticado> precosPraticados) {
        this.precosPraticados = precosPraticados;
    }

    public BigDecimal getPrecoPraticadoInput() { return precoPraticadoInput; }
    public void setPrecoPraticadoInput(BigDecimal v) { this.precoPraticadoInput = v; }
    public LocalDate getPrecoVigenciaInicioInput() { return precoVigenciaInicioInput; }
    public void setPrecoVigenciaInicioInput(LocalDate d) { this.precoVigenciaInicioInput = d; }

    

}
