package br.com.chatia.service;

import br.com.chatia.model.Usuario;
import br.com.chatia.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService() {
        usuarioRepository = new UsuarioRepository();
    }

    public void cadastrar(String nome, String email, String senha) {

        if (nome == null || nome.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || senha == null || senha.isEmpty()) {

            throw new IllegalArgumentException(
                    "Todos os campos precisam ser preenchidos!"
            );
        }

        nome = nome.trim();
        email = email.trim().toLowerCase();

        // Verifica se o e-mail é válido
        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Digite um e-mail válido."
            );
        }

        // Verifica se a senha tem pelo menos 8 caracteres
        if (senha.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha precisa ter pelo menos 8 caracteres."
            );
        }

        // Verifica se já existe uma conta com esse e-mail
        if (usuarioRepository.buscarPorEmail(email) != null) {
            throw new IllegalArgumentException(
                    "Já existe uma conta com esse e-mail!"
            );
        }

        // Criptografa a senha
        String senhaHash = BCrypt.hashpw(
                senha,
                BCrypt.gensalt()
        );

        // Cria e salva o usuário
        Usuario usuario = new Usuario(
                nome,
                email,
                senhaHash
        );

        usuarioRepository.salvar(usuario);
    }

    public Usuario autenticar(String email, String senha) {

        if (email == null || senha == null
                || email.trim().isEmpty() || senha.isEmpty()) {
            return null;
        }

        email = email.trim().toLowerCase();

        Usuario usuario = usuarioRepository.buscarPorEmail(email);

        if (usuario == null) {
            return null;
        }

        boolean senhaCorreta = BCrypt.checkpw(
                senha,
                usuario.getSenhaHash()
        );

        if (!senhaCorreta) {
            return null;
        }

        return usuario;
    }
    /** Regra de senha num lugar só, usada no cadastro e na redefinição. */
    public void validarSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha precisa ter pelo menos 8 caracteres."
            );
        }
    }

    public void redefinirSenha(String usuarioId, String novaSenha) {
        validarSenha(novaSenha);

        String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());

        if (!usuarioRepository.atualizarSenha(usuarioId, novoHash)) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }
    }
}
