package br.com.chatia.controller;

import br.com.chatia.service.PasswordRecoveryService;
import br.com.chatia.util.Estilos;
import br.com.chatia.util.Tarefa;
import br.com.chatia.view.LoginView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ForgotPasswordController {

    private final Stage stage;
    private final PasswordRecoveryService service = new PasswordRecoveryService();

    public ForgotPasswordController(Stage stage) {
        this.stage = stage;
    }

    /** aoConcluir recebe o código (modo demonstração) ou null se a conta não existe. */
    public void solicitarCodigo(String email, Label mensagem, Button botao,
                                Consumer<String> aoConcluir) {
        Estilos.limpar(mensagem);
        botao.setDisable(true);

        Tarefa.executar(
                () -> service.solicitar(email),
                codigo -> {
                    botao.setDisable(false);
                    aoConcluir.accept(codigo);
                },
                erro -> {
                    botao.setDisable(false);
                    Estilos.erro(mensagem, erro);
                }
        );
    }

    public void redefinir(String email, String codigo, String senha, String confirmar,
                          Label mensagem, Button botao) {

        if (codigo.isBlank() || senha.isEmpty() || confirmar.isEmpty()) {
            Estilos.erro(mensagem, "Preencha todos os campos.");
            return;
        }
        if (!senha.equals(confirmar)) {
            Estilos.erro(mensagem, "As senhas não são iguais.");
            return;
        }

        Estilos.limpar(mensagem);
        botao.setDisable(true);
        botao.setText("Redefinindo...");

        Tarefa.executar(
                () -> {
                    service.redefinirSenha(email, codigo, senha);
                    return null;
                },
                v -> new LoginView().show(stage, "Senha redefinida com sucesso! Faça login."),
                erro -> {
                    botao.setDisable(false);
                    botao.setText("Redefinir senha");
                    Estilos.erro(mensagem, erro);
                }
        );
    }

    public void voltar() {
        new LoginView().show(stage);
    }
}