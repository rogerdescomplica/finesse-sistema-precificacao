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
@Table(name = "fornecedores")
@EntityListeners(AuditingEntityListener.class)
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(length = 14, unique = true)
    private String cnpj;

    @Column(length = 11)
    private String cpf;

    @Column(length = 255)
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

    @Column(name = "prazo_pagamento")
    private Integer prazoPagamento = 0; // dias para pagamento

    @Column(name = "prazo_entrega")
    private Integer prazoEntrega = 0; // dias para entrega

    @Column(nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // Constructors
    public Fornecedor() {}

    public Fornecedor(String nome, Clinica clinica) {
        this.nome = nome;
        this.clinica = clinica;
    }

    // Builder
    public static class Builder {
        private UUID id;
        private Clinica clinica;
        private String nome;
        private String cnpj;
        private String cpf;
        private String email;
        private String telefone;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private Integer prazoPagamento;
        private Integer prazoEntrega;
        private Boolean ativo;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder clinica(Clinica clinica) {
            this.clinica = clinica;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder cnpj(String cnpj) {
            this.cnpj = cnpj;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public Builder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public Builder cidade(String cidade) {
            this.cidade = cidade;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder cep(String cep) {
            this.cep = cep;
            return this;
        }

        public Builder prazoPagamento(Integer prazoPagamento) {
            this.prazoPagamento = prazoPagamento;
            return this;
        }

        public Builder prazoEntrega(Integer prazoEntrega) {
            this.prazoEntrega = prazoEntrega;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Builder criadoEm(LocalDateTime criadoEm) {
            this.criadoEm = criadoEm;
            return this;
        }

        public Builder atualizadoEm(LocalDateTime atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
            return this;
        }

        public Fornecedor build() {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.id = this.id;
            fornecedor.clinica = this.clinica;
            fornecedor.nome = this.nome;
            fornecedor.cnpj = this.cnpj;
            fornecedor.cpf = this.cpf;
            fornecedor.email = this.email;
            fornecedor.telefone = this.telefone;
            fornecedor.endereco = this.endereco;
            fornecedor.cidade = this.cidade;
            fornecedor.estado = this.estado;
            fornecedor.cep = this.cep;
            fornecedor.prazoPagamento = this.prazoPagamento != null ? this.prazoPagamento : 0;
            fornecedor.prazoEntrega = this.prazoEntrega != null ? this.prazoEntrega : 0;
            fornecedor.ativo = this.ativo != null ? this.ativo : true;
            fornecedor.criadoEm = this.criadoEm;
            fornecedor.atualizadoEm = this.atualizadoEm;
            return fornecedor;
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

    public Clinica getClinica() {
        return clinica;
    }

    public void setClinica(Clinica clinica) {
        this.clinica = clinica;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
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

    public Integer getPrazoPagamento() {
        return prazoPagamento;
    }

    public void setPrazoPagamento(Integer prazoPagamento) {
        this.prazoPagamento = prazoPagamento != null ? prazoPagamento : 0;
    }

    public Integer getPrazoEntrega() {
        return prazoEntrega;
    }

    public void setPrazoEntrega(Integer prazoEntrega) {
        this.prazoEntrega = prazoEntrega != null ? prazoEntrega : 0;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo != null ? ativo : true;
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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fornecedor that = (Fornecedor) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Fornecedor{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}