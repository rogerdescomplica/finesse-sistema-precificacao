package com.finesse.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sessoes_usuario")
@EntityListeners(AuditingEntityListener.class)
public class SessaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_acesso", nullable = false, length = 500)
    private String tokenAcesso;

    @Column(name = "token_refresh", nullable = false, length = 500)
    private String tokenRefresh;

    @Column(name = "endereco_ip")
    private String enderecoIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "refresh_expira_em", nullable = false)
    private LocalDateTime refreshExpiraEm;

    @Column(name = "revogado", nullable = false)
    private Boolean revogado = false;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // Constructors
    public SessaoUsuario() {}

    public SessaoUsuario(Usuario usuario, String tokenAcesso, String tokenRefresh, 
                        LocalDateTime expiraEm, LocalDateTime refreshExpiraEm) {
        this.usuario = usuario;
        this.tokenAcesso = tokenAcesso;
        this.tokenRefresh = tokenRefresh;
        this.expiraEm = expiraEm;
        this.refreshExpiraEm = refreshExpiraEm;
    }

    // Builder
    public static class Builder {
        private UUID id;
        private Usuario usuario;
        private String tokenAcesso;
        private String tokenRefresh;
        private String enderecoIp;
        private String userAgent;
        private LocalDateTime expiraEm;
        private LocalDateTime refreshExpiraEm;
        private Boolean revogado;
        private LocalDateTime criadoEm;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder tokenAcesso(String tokenAcesso) {
            this.tokenAcesso = tokenAcesso;
            return this;
        }

        public Builder tokenRefresh(String tokenRefresh) {
            this.tokenRefresh = tokenRefresh;
            return this;
        }

        public Builder enderecoIp(String enderecoIp) {
            this.enderecoIp = enderecoIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder expiraEm(LocalDateTime expiraEm) {
            this.expiraEm = expiraEm;
            return this;
        }

        public Builder refreshExpiraEm(LocalDateTime refreshExpiraEm) {
            this.refreshExpiraEm = refreshExpiraEm;
            return this;
        }

        public Builder revogado(Boolean revogado) {
            this.revogado = revogado;
            return this;
        }

        public Builder criadoEm(LocalDateTime criadoEm) {
            this.criadoEm = criadoEm;
            return this;
        }

        public SessaoUsuario build() {
            SessaoUsuario sessao = new SessaoUsuario();
            sessao.id = this.id;
            sessao.usuario = this.usuario;
            sessao.tokenAcesso = this.tokenAcesso;
            sessao.tokenRefresh = this.tokenRefresh;
            sessao.enderecoIp = this.enderecoIp;
            sessao.userAgent = this.userAgent;
            sessao.expiraEm = this.expiraEm;
            sessao.refreshExpiraEm = this.refreshExpiraEm;
            sessao.revogado = this.revogado != null ? this.revogado : false;
            sessao.criadoEm = this.criadoEm;
            return sessao;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTokenAcesso() {
        return tokenAcesso;
    }

    public void setTokenAcesso(String tokenAcesso) {
        this.tokenAcesso = tokenAcesso;
    }

    public String getTokenRefresh() {
        return tokenRefresh;
    }

    public void setTokenRefresh(String tokenRefresh) {
        this.tokenRefresh = tokenRefresh;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public void setEnderecoIp(String enderecoIp) {
        this.enderecoIp = enderecoIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getRefreshExpiraEm() {
        return refreshExpiraEm;
    }

    public void setRefreshExpiraEm(LocalDateTime refreshExpiraEm) {
        this.refreshExpiraEm = refreshExpiraEm;
    }

    public Boolean getRevogado() {
        return revogado;
    }

    public void setRevogado(Boolean revogado) {
        this.revogado = revogado != null ? revogado : false;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // Métodos auxiliares
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    public boolean isRefreshExpirado() {
        return LocalDateTime.now().isAfter(refreshExpiraEm);
    }

    public boolean isValido() {
        return !revogado && !isExpirado();
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessaoUsuario that = (SessaoUsuario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "SessaoUsuario{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getEmail() : "null") +
                ", expiraEm=" + expiraEm +
                ", revogado=" + revogado +
                '}';
    }
}