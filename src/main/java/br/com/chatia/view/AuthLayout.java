package br.com.chatia.view;

import br.com.chatia.controller.ThemeController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Centraliza um cartão na janela e rola se a janela ficar pequena. */
final class AuthLayout {

    private AuthLayout() {
    }

    static ScrollPane criar(VBox card) {
        card.getStyleClass().add("card");
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        ThemeController temaController = new ThemeController();
        Button botaoTema = new Button(temaController.textoDoBotao());
        botaoTema.getStyleClass().add("botao-secundario");
        botaoTema.setOnAction(e -> temaController.alternar(botaoTema));
        StackPane.setAlignment(botaoTema, Pos.TOP_RIGHT);

        StackPane centro = new StackPane(card, botaoTema);
        centro.setMinHeight(Region.USE_PREF_SIZE);
        centro.setPadding(new Insets(24));
        centro.getStyleClass().add("auth-root");

        ScrollPane scroll = new ScrollPane(centro);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("auth-scroll");
        return scroll;
    }
}