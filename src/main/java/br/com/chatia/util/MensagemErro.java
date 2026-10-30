package br.com.chatia.util;

import br.com.chatia.exception.IAException;
import com.mongodb.MongoException;

/** Converte exceções técnicas em mensagens que o usuário entende. */
public final class MensagemErro {

    private MensagemErro() {
    }

    public static String de(Throwable erro) {
        // Mensagens de validação já são escritas para o usuário.
        if (erro instanceof IllegalArgumentException && erro.getMessage() != null) {
            return erro.getMessage();
        }

        // Erros da IA: a mensagem já é amigável; o detalhe vai só pro console.
        if (erro instanceof IAException) {
            System.err.println("[ERRO IA] " + erro.getMessage()
                    + (erro.getCause() != null ? " | causa: " + erro.getCause() : ""));
            return erro.getMessage();
        }

        // Detalhe técnico fica só no console.
        System.err.println("[ERRO] " + erro);
        erro.printStackTrace();

        if (erro instanceof IllegalStateException) {
            return "A configuração do banco de dados não foi encontrada. "
                    + "Verifique o arquivo de configuração.";
        }
        if (erro instanceof MongoException) {
            return "Não foi possível acessar o banco de dados. "
                    + "Verifique sua internet e as credenciais.";
        }
        return "Ocorreu um erro inesperado. Tente novamente.";
    }
}