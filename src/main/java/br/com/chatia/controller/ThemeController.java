package br.com.chatia.controller;

import br.com.chatia.util.Estilos;
import br.com.chatia.util.TransicaoTema;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class ThemeController {

    /** O botão mostra o tema para o qual ele vai trocar. */
    public String textoDoBotao() {
        return Estilos.temaAtual().outro().getNome();
    }

    public void alternar(Button botao) {
        Scene scene = botao.getScene();
        if (scene == null) {
            return;
        }
        // A luz nasce no botão e vai trocando o tema por onde passa
        TransicaoTema.executar(botao, () -> {
            Estilos.alternarTema(scene);
            botao.setText(textoDoBotao());
        });
    }
}