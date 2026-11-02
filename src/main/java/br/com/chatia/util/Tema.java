package br.com.chatia.util;

public enum Tema {

    // O 3º valor é a cor de fundo da aplicação, usada pela transição de telas
    ESCURO("/css/dark.css", "Tema escuro", "#0c1017"),
    CLARO("/css/light.css", "Tema claro", "#f1f6fb");

    private final String caminho;
    private final String nome;
    private final String corFundo;

    Tema(String caminho, String nome, String corFundo) {
        this.caminho = caminho;
        this.nome = nome;
        this.corFundo = corFundo;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getNome() {
        return nome;
    }

    public String getCorFundo() {
        return corFundo;
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