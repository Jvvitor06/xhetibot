package br.com.chatia.controller;

import br.com.chatia.service.UsuarioService;
import br.com.chatia.util.Estilos;
import br.com.chatia.util.MensagemErro;
import br.com.chatia.view.LoginView;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class RegisterController {

    private final Stage stage;
    private final UsuarioService usuarioService = new UsuarioService();

    public RegisterController(Stage stage) {
        this.stage = stage;
    }

    public void cadastrar(String nome, String email, String senha,
                          String confirmar, Label mensagem, Button botao) {

        if (nome.isBlank() || email.isBlank()
                || senha.isEmpty() || confirmar.isEmpty()) {
            Estilos.erro(mensagem, "Preencha todos os campos.");
            return;
        }
        if (!senha.equals(confirmar)) {
            Estilos.erro(mensagem, "As senhas não são iguais.");
            return;
        }

        Estilos.limpar(mensagem);
        ocupado(botao, true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() {
                usuarioService.cadastrar(nome, email, senha);
                return null;
            }
        };

        tarefa.setOnSucceeded(e ->
                new LoginView().show(stage, "Conta criada com sucesso! Faça login.")
        );

        tarefa.setOnFailed(e -> {
            Estilos.erro(mensagem, MensagemErro.de(tarefa.getException()));
            ocupado(botao, false);
        });

        Thread thread = new Thread(tarefa, "cadastro-task");
        thread.setDaemon(true);
        thread.start();
    }

    public void irParaLogin() {
        new LoginView().show(stage);
    }

    private void ocupado(Button botao, boolean ocupado) {
        botao.setDisable(ocupado);
        botao.setText(ocupado ? "Cadastrando..." : "Criar conta");
    }
}