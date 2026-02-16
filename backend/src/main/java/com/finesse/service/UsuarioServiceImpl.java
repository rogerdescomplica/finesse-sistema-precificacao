package com.finesse.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
import com.finesse.dto.AtualizarUsuarioRequestRecord;
import com.finesse.exception.NotFoundException;
import com.finesse.exception.ServiceOperationException;
import com.finesse.exception.ValidationException;
import com.finesse.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // construtor para unit tests
    public UsuarioServiceImpl(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario atualizarUsuario(Long id, AtualizarUsuarioRequestRecord request) {
        try {
            Usuario u = buscarPorId(id);
            if (request.nome() != null && !request.nome().trim().isEmpty()) {
                u.setNome(request.nome().trim());
            }
            if (request.email() != null && !request.email().trim().isEmpty()) {
                String novoEmail = request.email().trim().toLowerCase();
                if (!novoEmail.equals(u.getEmail())) {
                    throw new ValidationException("Alteração de email não suportada neste endpoint");
                }
            }
            if (request.perfil() != null) {
                try {
                    u.setPerfil(Perfil.valueOf(request.perfil().toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    throw new ValidationException("Perfil inválido: " + request.perfil());
                    }
                }
                if (u.getPerfil() == null) {
                    throw new ValidationException("Usuário deve possuir ao menos um perfil");
                }
            
            return repo.save(u);
        } catch (Exception ex) {
            if (ex instanceof ValidationException vex) throw vex;
            log.error("Falha ao atualizar usuário id={}", id, ex);
            throw new ServiceOperationException("Falha ao atualizar usuário", ex);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        try {
            return repo.findAll();
        } catch (Exception ex) {
            log.error("Falha ao listar todos usuários", ex);
            throw new ServiceOperationException("Falha ao listar usuários", ex);
        }
    }

    @Override
    public List<Usuario> listarAtivos() {
        try {
            return repo.findAll().stream().filter(Usuario::isEnabled).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Falha ao listar usuários ativos", ex);
            throw new ServiceOperationException("Falha ao listar usuários ativos", ex);
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    @Override
    public List<Usuario> buscarPorNome(String nome) {
        try {
            String q = nome == null ? "" : nome.trim().toLowerCase();
            return repo.findAll().stream()
                    .filter(u -> u.getNome() != null && u.getNome().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Falha ao buscar por nome", ex);
            throw new ServiceOperationException("Falha ao buscar por nome", ex);
        }
    }

    @Override
    @Transactional
    public Usuario criarUsuario(String nome, String email, String senha, Perfil perfil) {
        try {
            if (emailExiste(email)) {
                throw new ValidationException("Email já cadastrado");
            }
            if (!senhaForte(senha)) {
                throw new ValidationException("Senha fraca: mínimo 8 caracteres com maiúscula, minúscula, número e especial");
            }
            Usuario u = new Usuario();
            u.setNome(nome == null ? null : nome.trim());
            u.setEmail(email == null ? null : email.trim().toLowerCase());
            u.setSenha(passwordEncoder.encode(senha));
            u.adicionarPerfil(perfil != null ? perfil : Perfil.VISUALIZADOR);
            return repo.save(u);
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao criar usuário: {}", vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao criar usuário", ex);
            throw new ServiceOperationException("Falha ao criar usuário", ex);
        }
    }

    @Override
    @Transactional
    public void alterarSenha(Long id, String novaSenha) {
        try {
            if (!senhaForte(novaSenha)) {
                throw new ValidationException("Senha fraca: mínimo 8 caracteres com maiúscula, minúscula, número e especial");
            }
            Usuario u = buscarPorId(id);
            u.setSenha(passwordEncoder.encode(novaSenha));
            repo.save(u);
            log.info("Senha alterada para usuário id={}", id);
        } catch (ValidationException vex) {
            log.warn("Validação falhou ao alterar senha: {}", vex.getMessage());
            throw vex;
        } catch (Exception ex) {
            log.error("Falha ao alterar senha para usuário id={}", id, ex);
            throw new ServiceOperationException("Falha ao alterar senha", ex);
        }
    }

    @Override
    @Transactional
    public void deletarUsuario(Long id) {
        try {
            if (!repo.existsById(id)) {
                throw new NotFoundException("Usuário não encontrado");
            }
            repo.deleteById(id);
            log.info("Usuário deletado id={}", id);
        } catch (Exception ex) {
            log.error("Falha ao deletar usuário id={}", id, ex);
            if (ex instanceof NotFoundException nex) throw nex;
            throw new ServiceOperationException("Falha ao deletar usuário", ex);
        }
    }

    @Override
    public boolean emailExiste(String email) {
        try {
            return Boolean.TRUE.equals(repo.existsByEmail(email));
        } catch (Exception ex) {
            log.error("Falha ao verificar email existente", ex);
            throw new ServiceOperationException("Falha ao verificar email", ex);
        }
    }

    private boolean senhaForte(String senha) {
        if (senha == null) return false;
        String s = senha.trim();
        if (s.length() < 8) return false;
        boolean hasUpper = s.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = s.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = s.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = s.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",.<>/?`~".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    @Override
    public long contarTodos() {
        try {
            return repo.count();
        } catch (Exception ex) {
            log.error("Falha ao contar usuários", ex);
            throw new ServiceOperationException("Falha ao contar usuários", ex);
        }
    }

    @Override
    public long contarAtivos() {
        try {
            return repo.findAll().stream().filter(Usuario::isEnabled).count();
        } catch (Exception ex) {
            log.error("Falha ao contar usuários ativos", ex);
            throw new ServiceOperationException("Falha ao contar usuários ativos", ex);
        }
    }

    @Override
    @Transactional
    public Optional<Usuario> toggleStatus(Long id, boolean ativo) {
        try {
            return repo.findById(id).map(u -> {
                u.setAtivo(ativo);
                Usuario saved = repo.save(u);
                log.info("Status do usuário alterado id={} ativo={}", saved.getId(), ativo);
                return saved;
            });
        } catch (Exception ex) {
            log.error("Falha ao alternar status do usuário id={}", id, ex);
            throw new ServiceOperationException("Falha ao alternar status do usuário", ex);
        }
    }
}
