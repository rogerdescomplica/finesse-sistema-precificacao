package com.finesse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Entidade que representa os usuários do sistema
 * Implementa UserDetails para integração com Spring Security
 * Login é feito com email
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 32)
    private Perfil perfil = Perfil.VISUALIZADOR;

    @Column(name = "ativo")
    private Boolean ativo = true;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @PrePersist
    protected void onCreate() {
        // Se não tiver perfil definido, adiciona VISUALIZADOR por padrão
        if (perfil == null) {
            perfil = Perfil.VISUALIZADOR;
        }
    }

    // Implementação do UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(perfil.getRole()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email; // Email é usado como username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo != null && ativo;
    }

    // Métodos auxiliares
    public void adicionarPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public void removerPerfil(Perfil perfil) {
        this.perfil = Perfil.VISUALIZADOR;
    }

    public boolean temPerfil(Perfil perfil) {
        return this.perfil == perfil;
    }

    public boolean isAdmin() {
        return temPerfil(Perfil.ADMIN);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
