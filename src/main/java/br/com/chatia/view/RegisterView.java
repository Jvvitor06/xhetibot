package br.com.chatia.view;

import br.com.chatia.controller.RegisterController;
import br.com.chatia.util.Estilos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView {

    public void show(Stage stage) {
        RegisterController controller = new RegisterController(stage);

        Label eyebrow = texto("CRIE SUA CONTA", "eyebrow");
        Label titulo = texto("Cadastre-se", "titulo");
        Label descricao = texto("Comece sua conversa com a IA.", "subtitulo");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Seu nome");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("voce@exemplo.com");

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Mínimo de 8 caracteres");

        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Digite a senha novamente");

        Button criar = new Button("Criar conta");
        criar.getStyleClass().add("botao-primario");
        criar.setMaxWidth(Double.MAX_VALUE);
        criar.setDefaultButton(true);

        Label mensagem = new Label();
        mensagem.setWrapText(true);

        Button voltar = new Button("Já tem conta? Entrar");
        voltar.getStyleClass().add("link");
        voltar.setOnAction(e -> controller.irParaLogin());

        criar.setOnAction(e -> controller.cadastrar(
                txtNome.getText(), txtEmail.getText(),
                txtSenha.getText(), txtConfirmar.getText(),
                mensagem, criar
        ));

        VBox card = new VBox(8,
                eyebrow, titulo, descricao,
                texto("Nome", "rotulo"), txtNome,
                texto("E-mail", "rotulo"), txtEmail,
                texto("Senha", "rotulo"), txtSenha,
                texto("Confirmar senha", "rotulo"), txtConfirmar,
                criar, mensagem, voltar
        );

        Scene scene = new Scene(AuthLayout.criar(card), 900, 620);
        Estilos.aplicar(scene);

        stage.setTitle("Chat IA | Cadastro");
        stage.setMinWidth(520);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    private Label texto(String conteudo, String classe) {
        Label label = new Label(conteudo);
        label.getStyleClass().add(classe);
        return label;
    }
}