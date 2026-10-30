package br.com.chatia.model;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String senhaHash;
    public Usuario(){

    }
    public Usuario(String nome, String email, String senhaHash){
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

}

