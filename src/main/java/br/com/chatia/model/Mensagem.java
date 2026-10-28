package br.com.chatia.model;

import java.time.Instant;

public class Mensagem {

    private String id;
    private String usuarioId;
    private String conversaId;
    private AutorMensagem autor;
    private String conteudo;
    private Instant criadaEm;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getConversaId() { return conversaId; }
    public void setConversaId(String conversaId) { this.conversaId = conversaId; }

    public AutorMensagem getAutor() { return autor; }
    public void setAutor(AutorMensagem autor) { this.autor = autor; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant criadaEm) { this.criadaEm = criadaEm; }
}