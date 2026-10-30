package br.com.chatia.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

/** Define o ícone da janela (barra de título e barra de tarefas). */
public final class Icones {

    private static final String CAMINHO = "/images/chat-ia.png";

    private static Image icone;

    private Icones() {
    }

    public static void aplicar(Stage stage) {
        if (icone == null) {
            InputStream entrada = Icones.class.getResourceAsStream(CAMINHO);
            if (entrada == null) {
                System.err.println("Icone nao encontrado em resources: " + CAMINHO);
                return;
            }
            icone = new Image(entrada);
        }
        if (stage.getIcons().isEmpty()) {
            stage.getIcons().add(icone);
        }
    }
}