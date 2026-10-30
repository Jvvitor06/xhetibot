package br.com.chatia.controller;

import br.com.chatia.util.Estilos;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class ThemeController {

    /** O botão mostra o tema para o qual ele vai trocar. */
    public String textoDoBotao() {
        return Estilos.temaAtual().outro().getNome();
    }

    public void alternar(Button botao) {
        Scene scene = botao.getScene();
        if (scene != null) {
            Estilos.alternarTema(scene);
        }
        botao.setText(textoDoBotao());
    }
}