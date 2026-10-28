package br.com.chatia.util;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

import java.util.List;
import java.util.prefs.Preferences;

public final class Estilos {

    private static final String BASE = "/css/base.css";
    private static final String CHAVE_TEMA = "tema";

    // O tema escolhido é lido ao iniciar e fica guardado entre execuções
    private static Tema temaAtual = carregarTema();

    private Estilos() {
    }

    public static Tema temaAtual() {
        return temaAtual;
    }

    /** Troca o tema, salva a escolha e reaplica as folhas de estilo na tela. */
    public static Tema alternarTema(Scene scene) {
        temaAtual = temaAtual.outro();
        salvarTema(temaAtual);
        aplicar(scene);
        return temaAtual;
    }

    public static void aplicar(Scene scene) {
        scene.getStylesheets().setAll(folhas());
    }

    /** Janelas de diálogo (confirmar, renomear) também usam o tema. */
    public static void aplicar(DialogPane painel) {
        painel.getStylesheets().setAll(folhas());
    }

    public static void erro(Label label, String texto) {
        definir(label, "mensagem-erro", texto);
    }

    public static void ok(Label label, String texto) {
        definir(label, "mensagem-ok", texto);
    }

    public static void limpar(Label label) {
        definir(label, null, "");
    }

    // ------------------------------------------------------------------

    private static List<String> folhas() {
        return List.of(url(BASE), url(temaAtual.getCaminho()));
    }

    private static String url(String caminho) {
        return Estilos.class.getResource(caminho).toExternalForm();
    }

    private static Tema carregarTema() {
        try {
            String salvo = Preferences.userNodeForPackage(Estilos.class)
                    .get(CHAVE_TEMA, Tema.ESCURO.name());
            return Tema.de(salvo);
        } catch (RuntimeException e) {
            return Tema.ESCURO;
        }
    }

    private static void salvarTema(Tema tema) {
        try {
            Preferences.userNodeForPackage(Estilos.class).put(CHAVE_TEMA, tema.name());
        } catch (RuntimeException e) {
            System.err.println("Não foi possível salvar o tema: " + e);
        }
    }

    private static void definir(Label label, String classe, String texto) {
        label.getStyleClass().removeAll("mensagem-erro", "mensagem-ok");
        if (classe != null) {
            label.getStyleClass().add(classe);
        }
        label.setText(texto);
    }
}