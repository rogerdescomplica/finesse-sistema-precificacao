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
@Table(name = "clinicas")
@EntityListeners(AuditingEntityListener.class)
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 8)
    private String cep;

    @Column(name = "markup_padrao", precision = 5, scale = 2)
    private BigDecimal markupPadrao = new BigDecimal("30.00"); // 30% padrão

    @Column(name = "despesas_fixas_mensais", precision = 10, scale = 2)
    private BigDecimal despesasFixasMensais;

    @Column(name = "custo_hora_trabalho", precision = 10, scale = 2)
    private BigDecimal custoHoraTrabalho;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // Construtores
    public Clinica() {}

    public Clinica(UUID id, String nome, String cnpj, String email, String telefone, String endereco,
                   String cidade, String estado, String cep, BigDecimal markupPadrao, 
                   BigDecimal despesasFixasMensais, BigDecimal custoHoraTrabalho, 
                   LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.markupPadrao = markupPadrao != null ? markupPadrao : new BigDecimal("30.00");
        this.despesasFixasMensais = despesasFixasMensais;
        this.custoHoraTrabalho = custoHoraTrabalho;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    // Builder estático
    public static ClinicaBuilder builder() {
        return new ClinicaBuilder();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public BigDecimal getMarkupPadrao() {
        return markupPadrao;
    }

    public void setMarkupPadrao(BigDecimal markupPadrao) {
        this.markupPadrao = markupPadrao != null ? markupPadrao : new BigDecimal("30.00");
    }

    public BigDecimal getDespesasFixasMensais() {
        return despesasFixasMensais;
    }

    public void setDespesasFixasMensais(BigDecimal despesasFixasMensais) {
        this.despesasFixasMensais = despesasFixasMensais;
    }

    public BigDecimal getCustoHoraTrabalho() {
        return custoHoraTrabalho;
    }

    public void setCustoHoraTrabalho(BigDecimal custoHoraTrabalho) {
        this.custoHoraTrabalho = custoHoraTrabalho;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clinica clinica = (Clinica) o;
        return Objects.equals(id, clinica.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Clinica{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", email='" + email + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }

    // Builder interno
    public static class ClinicaBuilder {
        private UUID id;
        private String nome;
        private String cnpj;
        private String email;
        private String telefone;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private BigDecimal markupPadrao = new BigDecimal("30.00");
        private BigDecimal despesasFixasMensais;
        private BigDecimal custoHoraTrabalho;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;

        ClinicaBuilder() {}

        public ClinicaBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ClinicaBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public ClinicaBuilder cnpj(String cnpj) {
            this.cnpj = cnpj;
            return this;
        }

        public ClinicaBuilder email(String email) {
            this.email = email;
            return this;
        }

        public ClinicaBuilder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public ClinicaBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public ClinicaBuilder cidade(String cidade) {
            this.cidade = cidade;
            return this;
        }

        public ClinicaBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public ClinicaBuilder cep(String cep) {
            this.cep = cep;
            return this;
        }

        public ClinicaBuilder markupPadrao(BigDecimal markupPadrao) {
            this.markupPadrao = markupPadrao;
            return this;
        }

        public ClinicaBuilder despesasFixasMensais(BigDecimal despesasFixasMensais) {
            this.despesasFixasMensais = despesasFixasMensais;
            return this;
        }

        public ClinicaBuilder custoHoraTrabalho(BigDecimal custoHoraTrabalho) {
            this.custoHoraTrabalho = custoHoraTrabalho;
            return this;
        }

        public ClinicaBuilder criadoEm(LocalDateTime criadoEm) {
            this.criadoEm = criadoEm;
            return this;
        }

        public ClinicaBuilder atualizadoEm(LocalDateTime atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
            return this;
        }

        public Clinica build() {
            return new Clinica(id, nome, cnpj, email, telefone, endereco, cidade, estado, 
                              cep, markupPadrao, despesasFixasMensais, custoHoraTrabalho, 
                              criadoEm, atualizadoEm);
        }
    }
}