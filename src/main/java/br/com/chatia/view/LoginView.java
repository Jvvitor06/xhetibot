package br.com.chatia.view;

import br.com.chatia.controller.LoginController;
import br.com.chatia.util.Estilos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    public void show(Stage stage) {
        show(stage, null);
    }

    public void show(Stage stage, String avisoSucesso) {
        LoginController controller = new LoginController(stage);

        Label eyebrow = texto("BEM-VINDO DE VOLTA", "eyebrow");
        Label titulo = texto("Entrar", "titulo");
        Label descricao = texto("Acesse sua conta para continuar.", "subtitulo");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("voce@exemplo.com");

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Sua senha");

        Button entrar = new Button("Entrar");
        entrar.getStyleClass().add("botao-primario");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setDefaultButton(true);

        Label mensagem = new Label();
        mensagem.setWrapText(true);
        if (avisoSucesso != null) {
            Estilos.ok(mensagem, avisoSucesso);
        }

        Button esqueci = new Button("Esqueci minha senha");
        esqueci.getStyleClass().add("link");
        esqueci.setOnAction(e -> controller.irParaRecuperacao());
        Button criarConta = new Button("Não tem conta? Cadastre-se");
        criarConta.getStyleClass().add("link");
        criarConta.setOnAction(e -> controller.irParaCadastro());

        entrar.setOnAction(e -> controller.entrar(
                txtEmail.getText(), txtSenha.getText(), mensagem, entrar
        ));

        VBox card = new VBox(8,
                eyebrow, titulo, descricao,
                texto("E-mail", "rotulo"), txtEmail,
                texto("Senha", "rotulo"), txtSenha,
                esqueci, entrar, mensagem, criarConta
        );

        Scene scene = new Scene(AuthLayout.criar(card), 900, 620);
        Estilos.aplicar(scene);

        stage.setTitle("Chat IA | Login");
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