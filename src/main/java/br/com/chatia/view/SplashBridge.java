package br.com.chatia.view;

import javafx.application.Platform;
import javafx.stage.Stage;
import br.com.chatia.util.Icones;

public class SplashBridge {

    private final Stage splashStage;
    private boolean concluido = false;

    public SplashBridge(Stage splashStage) {
        this.splashStage = splashStage;
    }

    /** Chamado pelo JavaScript ao fim da animação. */
    public void finalizar() {
        Platform.runLater(() -> {
            if (concluido) {
                return;
            }
            concluido = true;

            // Abre o login ANTES de fechar o splash, senão o JavaFX
            // encerra a aplicação ao fechar a última janela.
            Stage principal = new Stage();
            Icones.aplicar(principal);
            new LoginView().show(principal);
            splashStage.close();
        });
    }
}