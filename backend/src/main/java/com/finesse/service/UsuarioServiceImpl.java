package com.finesse.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;
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
            if (senha == null || senha.trim().length() < 6) {
                throw new ValidationException("Senha deve possuir no mínimo 6 caracteres");
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
            if (novaSenha == null || novaSenha.trim().length() < 6) {
                throw new ValidationException("Senha deve possuir no mínimo 6 caracteres");
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
    public Usuario ativarUsuario(Long id) {
        try {
            Usuario u = buscarPorId(id);
            if (Boolean.TRUE.equals(u.getAtivo())) {
                throw new ValidationException("Usuário já está ativo");
            }
            u.setAtivo(Boolean.TRUE);
            Usuario saved = repo.save(u);
            log.info("Usuário ativado id={}", id);
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao ativar usuário id={}", id, ex);
            if (ex instanceof ValidationException vex) throw vex;
            throw new ServiceOperationException("Falha ao ativar usuário", ex);
        }
    }

    @Override
    @Transactional
    public Usuario desativarUsuario(Long id) {
        try {
            Usuario u = buscarPorId(id);
            if (Boolean.FALSE.equals(u.getAtivo())) {
                throw new ValidationException("Usuário já está inativo");
            }
            u.setAtivo(Boolean.FALSE);
            Usuario saved = repo.save(u);
            log.info("Usuário desativado id={}", id);
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao desativar usuário id={}", id, ex);
            if (ex instanceof ValidationException vex) throw vex;
            throw new ServiceOperationException("Falha ao desativar usuário", ex);
        }
    }

    @Override
    @Transactional
    public Usuario adicionarPerfil(Long id, Perfil perfil) {
        try {
            Usuario u = buscarPorId(id);
            if (u.temPerfil(perfil)) {
                throw new ValidationException("Usuário já possui o perfil");
            }
            u.adicionarPerfil(perfil);
            Usuario saved = repo.save(u);
            log.info("Perfil {} adicionado para usuário id={}", perfil, id);
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao adicionar perfil para usuário id={}", id, ex);
            if (ex instanceof ValidationException vex) throw vex;
            throw new ServiceOperationException("Falha ao adicionar perfil", ex);
        }
    }

    @Override
    @Transactional
    public Usuario removerPerfil(Long id, Perfil perfil) {
        try {
            Usuario u = buscarPorId(id);
            if (!u.temPerfil(perfil)) {
                throw new ValidationException("Usuário não possui o perfil informado");
            }
            if (u.getPerfis().size() == 1) {
                throw new ValidationException("Usuário deve possuir ao menos um perfil");
            }
            u.removerPerfil(perfil);
            Usuario saved = repo.save(u);
            log.info("Perfil {} removido do usuário id={}", perfil, id);
            return saved;
        } catch (Exception ex) {
            log.error("Falha ao remover perfil do usuário id={}", id, ex);
            if (ex instanceof ValidationException vex) throw vex;
            throw new ServiceOperationException("Falha ao remover perfil", ex);
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
}
