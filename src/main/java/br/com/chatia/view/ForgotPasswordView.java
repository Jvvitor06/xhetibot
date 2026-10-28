package br.com.chatia.view;

import br.com.chatia.controller.ForgotPasswordController;
import br.com.chatia.service.PasswordRecoveryService;
import br.com.chatia.util.Estilos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ForgotPasswordView {

    public void show(Stage stage) {
        ForgotPasswordController controller = new ForgotPasswordController(stage);

        Label eyebrow = texto("RECUPERAÇÃO DE SENHA", "eyebrow");
        Label titulo = texto("Esqueci minha senha", "titulo");
        Label descricao = texto(
                "Informe o e-mail da sua conta para gerar um código temporário.", "subtitulo");

        // ---------- Etapa 1: e-mail ----------
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("voce@exemplo.com");

        Button botaoGerar = new Button("Gerar código");
        botaoGerar.getStyleClass().add("botao-primario");
        botaoGerar.setMaxWidth(Double.MAX_VALUE);

        // ---------- Etapa 2: código + nova senha (escondida no início) ----------
        Label lblCodigoDemo = new Label();
        lblCodigoDemo.getStyleClass().add("mensagem-ok");
        lblCodigoDemo.setStyle("-fx-font-weight: bold;");
        lblCodigoDemo.setWrapText(true);
        mostrar(lblCodigoDemo, false);

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex.: ABCD-2345");

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Mínimo de 8 caracteres");

        PasswordField txtConfirmar = new PasswordField();
        txtConfirmar.setPromptText("Digite a nova senha novamente");

        Button botaoRedefinir = new Button("Redefinir senha");
        botaoRedefinir.getStyleClass().add("botao-primario");
        botaoRedefinir.setMaxWidth(Double.MAX_VALUE);

        VBox etapa2 = new VBox(8,
                lblCodigoDemo,
                texto("Código", "rotulo"), txtCodigo,
                texto("Nova senha", "rotulo"), txtSenha,
                texto("Confirmar nova senha", "rotulo"), txtConfirmar,
                botaoRedefinir
        );
        mostrar(etapa2, false);

        Label mensagem = new Label();
        mensagem.setWrapText(true);

        Button voltar = new Button("Voltar ao login");
        voltar.getStyleClass().add("link");
        voltar.setOnAction(e -> controller.voltar());

        // ---------- Ações ----------
        botaoGerar.setOnAction(e -> controller.solicitarCodigo(
                txtEmail.getText(), mensagem, botaoGerar, codigo -> {
                    mostrar(etapa2, true);
                    botaoGerar.setText("Gerar novo código");
                    Estilos.ok(mensagem,
                            "Se o e-mail estiver cadastrado, um código temporário foi gerado.");

                    if (codigo != null) {
                        lblCodigoDemo.setText("MODO DEMONSTRAÇÃO. Seu código: " + codigo
                                + "  (válido por " + PasswordRecoveryService.VALIDADE_MINUTOS
                                + " minutos)");
                        mostrar(lblCodigoDemo, true);
                    } else {
                        mostrar(lblCodigoDemo, false);
                    }
                    txtCodigo.requestFocus();
                }
        ));
        txtEmail.setOnAction(e -> botaoGerar.fire());

        botaoRedefinir.setOnAction(e -> controller.redefinir(
                txtEmail.getText(), txtCodigo.getText(),
                txtSenha.getText(), txtConfirmar.getText(),
                mensagem, botaoRedefinir
        ));
        txtConfirmar.setOnAction(e -> botaoRedefinir.fire());

        VBox card = new VBox(8,
                eyebrow, titulo, descricao,
                texto("E-mail", "rotulo"), txtEmail,
                botaoGerar,
                etapa2,
                mensagem,
                voltar
        );

        Scene scene = new Scene(AuthLayout.criar(card), 900, 700);
        Estilos.aplicar(scene);

        stage.setTitle("Chat IA | Recuperar senha");
        stage.setMinWidth(520);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    private void mostrar(javafx.scene.Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }

    private Label texto(String conteudo, String classe) {
        Label label = new Label(conteudo);
        label.getStyleClass().add(classe);
        return label;
    }
}