package br.com.chatia;

/**
 * Ponto de entrada do aplicativo instalado.
 * Não estende Application de propósito: quando o JavaFX está no classpath
 * (como no pacote do jpackage), a classe principal não pode herdar de Application.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}