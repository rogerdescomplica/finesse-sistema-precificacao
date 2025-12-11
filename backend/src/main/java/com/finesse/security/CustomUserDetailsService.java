package com.finesse.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Usuario;
import com.finesse.repository.UsuarioRepository;

/**
 * Serviço customizado para carregar detalhes do usuário do banco de dados
 * Implementa UserDetailsService do Spring Security
 * 
 * IMPORTANTE: No sistema Finesse, o login é feito com EMAIL, não username
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carrega o usuário pelo email (usado como username no sistema)
     * Este método é chamado automaticamente pelo AuthenticationProvider
     * 
     * @param email o email do usuário (username do sistema)
     * @return UserDetails (a própria entidade Usuario implementa esta interface)
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar usuário no banco de dados pelo email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuário não encontrado com email: " + email
                ));

        // Verificar se o usuário está ativo
        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Usuário desativado: " + email);
        }

        // Retornar o próprio Usuario (que já implementa UserDetails)
        return usuario;
    }

    /**
     * Método auxiliar para carregar usuário por ID
     * Útil para validação de token JWT
     * 
     * @param id o ID do usuário
     * @return UserDetails contendo as informações do usuário
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuário não encontrado com id: " + id
                ));

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Usuário desativado com id: " + id);
        }

        return usuario;
    }
}