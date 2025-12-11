package com.finesse.service;

import java.util.List;

import com.finesse.entity.Perfil;
import com.finesse.entity.Usuario;

/**
 * Interface de serviço para gerenciamento de usuários
 * Define o contrato de operações disponíveis
 */
public interface UsuarioService {

    /**
     * Listar todos os usuários (ativos e inativos)
     * 
     * @return Lista de todos os usuários
     */
    List<Usuario> listarTodos();

    /**
     * Listar apenas usuários ativos
     * 
     * @return Lista de usuários ativos
     */
    List<Usuario> listarAtivos();

    /**
     * Buscar usuário por ID
     * 
     * @param id ID do usuário
     * @return Usuário encontrado
     * @throws RuntimeException se usuário não for encontrado
     */
    Usuario buscarPorId(Long id);

    /**
     * Buscar usuário por email
     * 
     * @param email Email do usuário
     * @return Usuário encontrado
     * @throws RuntimeException se usuário não for encontrado
     */
    Usuario buscarPorEmail(String email);

    /**
     * Buscar usuários por nome (busca parcial, case-insensitive)
     * 
     * @param nome Nome ou parte do nome a buscar
     * @return Lista de usuários encontrados
     */
    List<Usuario> buscarPorNome(String nome);

    /**
     * Criar novo usuário
     * 
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param senha Senha do usuário (será criptografada)
     * @param perfil Perfil inicial do usuário
     * @return Usuário criado
     * @throws RuntimeException se dados forem inválidos ou email já existir
     */
    Usuario criarUsuario(String nome, String email, String senha, Perfil perfil);

    /**
     * Atualizar usuário existente
     * 
     * @param id ID do usuário
     * @param form Form com dados para atualizar
     * @return Usuário atualizado
     * @throws RuntimeException se usuário não for encontrado ou dados inválidos
    
    Usuario atualizarUsuario(Long id, AtualizarUsuarioForm form);
    */

    /**
     * Alterar senha de um usuário
     * 
     * @param id ID do usuário
     * @param novaSenha Nova senha (será criptografada)
     * @throws RuntimeException se usuário não for encontrado ou senha inválida
     */
    void alterarSenha(Long id, String novaSenha);

    /**
     * Ativar um usuário desativado
     * 
     * @param id ID do usuário
     * @return Usuário ativado
     * @throws RuntimeException se usuário não for encontrado ou já estiver ativo
     */
    Usuario ativarUsuario(Long id);

    /**
     * Desativar um usuário ativo
     * 
     * @param id ID do usuário
     * @return Usuário desativado
     * @throws RuntimeException se usuário não for encontrado ou já estiver desativado
     */
    Usuario desativarUsuario(Long id);

    /**
     * Adicionar perfil ao usuário
     * 
     * @param id ID do usuário
     * @param perfil Perfil a ser adicionado
     * @return Usuário com perfil adicionado
     * @throws RuntimeException se usuário não for encontrado ou já tiver o perfil
     */
    Usuario adicionarPerfil(Long id, Perfil perfil);

    /**
     * Remover perfil do usuário
     * 
     * @param id ID do usuário
     * @param perfil Perfil a ser removido
     * @return Usuário com perfil removido
     * @throws RuntimeException se usuário não tiver o perfil ou for o único perfil
     */
    Usuario removerPerfil(Long id, Perfil perfil);

    /**
     * Deletar usuário permanentemente (exclusão física)
     * 
     * @param id ID do usuário
     * @throws RuntimeException se usuário não for encontrado
     */
    void deletarUsuario(Long id);

    /**
     * Verificar se email já existe no sistema
     * 
     * @param email Email a verificar
     * @return true se email existe, false caso contrário
     */
    boolean emailExiste(String email);

    /**
     * Contar total de usuários
     * 
     * @return Quantidade total de usuários
     */
    long contarTodos();

    /**
     * Contar usuários ativos
     * 
     * @return Quantidade de usuários ativos
     */
    long contarAtivos();
}