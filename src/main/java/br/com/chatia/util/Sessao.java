package br.com.chatia.util;
import br.com.chatia.model.Usuario;
public class Sessao {

    private static Usuario usuario;
    public static void iniciar(Usuario usuarioLogado){
        usuario = usuarioLogado;
    }
    public static Usuario getUsuario(){
        return usuario;
    }
    public static boolean estaLogado(){
        return usuario !=null;
    }
    public static void encerrar(){
        usuario = null;
    }
}
