package com.finesse.repository;

import com.finesse.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de banco de dados relacionadas a Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca usuário por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o email informado
     */
    Boolean existsByEmail(String email);

    /**
     * Busca usuário por email e ativo = true
     */
    Optional<Usuario> findByEmailAndAtivoTrue(String email);
}