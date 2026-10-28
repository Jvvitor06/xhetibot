package br.com.chatia.model;

import java.time.Instant;
import java.util.List;

public class Documento {

    private String id;
    private String titulo;
    private String conteudo;
    private List<String> palavrasChave;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public List<String> getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(List<String> palavrasChave) { this.palavrasChave = palavrasChave; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}