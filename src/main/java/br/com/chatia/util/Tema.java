package br.com.chatia.util;

public enum Tema {

    ESCURO("/css/dark.css", "Tema escuro"),
    CLARO("/css/light.css", "Tema claro");

    private final String caminho;
    private final String nome;

    Tema(String caminho, String nome) {
        this.caminho = caminho;
        this.nome = nome;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getNome() {
        return nome;
    }

    /** O tema para o qual o usuário pode alternar. */
    public Tema outro() {
        return this == ESCURO ? CLARO : ESCURO;
    }

    /** Converte o texto salvo em um Tema; se for inválido, usa o escuro. */
    public static Tema de(String texto) {
        try {
            return Tema.valueOf(texto);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ESCURO;
        }
    }
}