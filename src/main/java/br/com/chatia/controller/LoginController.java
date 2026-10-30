package br.com.chatia.controller;

import br.com.chatia.model.Usuario;
import br.com.chatia.service.UsuarioService;
import br.com.chatia.util.Estilos;
import br.com.chatia.util.MensagemErro;
import br.com.chatia.util.Sessao;
import br.com.chatia.view.ChatView;
import br.com.chatia.view.RegisterView;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import br.com.chatia.view.ForgotPasswordView;

public class LoginController {

    private final Stage stage;
    private final UsuarioService usuarioService = new UsuarioService();

    public LoginController(Stage stage) {
        this.stage = stage;
    }

    public void entrar(String email, String senha, Label mensagem, Button botao) {
        if (email.isBlank() || senha.isEmpty()) {
            Estilos.erro(mensagem, "Preencha todos os campos.");
            return;
        }

        Estilos.limpar(mensagem);
        ocupado(botao, true);

        // Consulta ao banco fora da thread da interface, para a janela não travar.
        Task<Usuario> tarefa = new Task<>() {
            @Override
            protected Usuario call() {
                return usuarioService.autenticar(email.trim(), senha);
            }
        };

        tarefa.setOnSucceeded(e -> {
            Usuario usuario = tarefa.getValue();
            if (usuario == null) {
                Estilos.erro(mensagem, "E-mail ou senha incorretos.");
                ocupado(botao, false);
                return;
            }
            Sessao.iniciar(usuario);
            new ChatView().show(stage);
        });

        tarefa.setOnFailed(e -> {
            Estilos.erro(mensagem, MensagemErro.de(tarefa.getException()));
            ocupado(botao, false);
        });

        Thread thread = new Thread(tarefa, "login-task");
        thread.setDaemon(true);
        thread.start();
    }

    public void irParaCadastro() {
        new RegisterView().show(stage);
    }

    public void irParaRecuperacao() {
        new ForgotPasswordView().show(stage);
    }

    private void ocupado(Button botao, boolean ocupado) {
        botao.setDisable(ocupado);
        botao.setText(ocupado ? "Entrando..." : "Entrar");
    }
}