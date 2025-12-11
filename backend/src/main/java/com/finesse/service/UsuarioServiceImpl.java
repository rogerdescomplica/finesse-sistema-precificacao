package com.finesse.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Override
    public Usuario adicionarPerfil(Long id, Perfil perfil) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void alterarSenha(Long id, String novaSenha) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Usuario ativarUsuario(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usuario buscarPorId(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Usuario> buscarPorNome(String nome) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long contarAtivos() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long contarTodos() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Usuario criarUsuario(String nome, String email, String senha, Perfil perfil) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deletarUsuario(Long id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Usuario desativarUsuario(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean emailExiste(String email) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Usuario> listarAtivos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usuario removerPerfil(Long id, Perfil perfil) {
        // TODO Auto-generated method stub
        return null;
    }
    
        
}