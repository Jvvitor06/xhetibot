package br.com.chatia.view;

import javafx.animation.PauseTransition;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;

/**
 * Tela de abertura: WebView carregando HTML + CSS + JS animado com GSAP.
 * Ao fim da animação o JavaScript avisa o Java e a tela de login é aberta.
 */
public class splashView {

    // Mensagem que o JavaScript envia (alert) quando a animação termina
    private static final String SINAL_FIM = "CHATIA_FINALIZAR";
    private static final double SEGURANCA_SEGUNDOS = 20;

    private SplashBridge bridge;

    public void show() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine webEngine = webView.getEngine();

        bridge = new SplashBridge(stage);

        // O JavaScript chama alert("CHATIA_FINALIZAR") no fim da animação.
        // Não usa JSObject nem executeScript: evita a ponte Java<->JS do WebKit,
        // que derrubava o aplicativo empacotado.
        webEngine.setOnAlert(evento -> {
            if (SINAL_FIM.equals(evento.getData())) {
                bridge.finalizar();
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener(
                (observable, antigo, novo) -> {
                    if (novo == Worker.State.FAILED) {
                        System.err.println("Erro ao carregar o HTML do splash.");
                        bridge.finalizar(); // não deixa o usuário preso no splash
                    }
                }
        );

        URL url = getClass().getResource("/splash/splash.html");
        if (url == null) {
            throw new IllegalStateException(
                    "Arquivo splash.html não encontrado em resources/splash."
            );
        }
        webEngine.load(url.toExternalForm());

        stage.setScene(new Scene(webView, 700, 420));
        stage.setTitle("Chat IA");
        stage.centerOnScreen();
        stage.show();

        // Trava de segurança: se algo travar, abre o login mesmo assim
        PauseTransition seguranca = new PauseTransition(Duration.seconds(SEGURANCA_SEGUNDOS));
        seguranca.setOnFinished(e -> bridge.finalizar());
        seguranca.play();
    }
}