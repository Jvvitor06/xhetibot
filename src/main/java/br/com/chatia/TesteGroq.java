package br.com.chatia;

import br.com.chatia.exception.IAException;
import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Mensagem;
import br.com.chatia.service.GroqService;

import java.util.List;

/** Rode: botão direito -> Run 'TesteGroq.main()'. */
public class TesteGroq {

    public static void main(String[] args) {
        Mensagem pergunta = new Mensagem();
        pergunta.setAutor(AutorMensagem.USUARIO);
        pergunta.setConteudo("Em uma frase curta: o que é encapsulamento em Java?");

        try {
            String resposta = new GroqService().responder(
                    "Você responde em português do Brasil, de forma curta.",
                    List.of(pergunta)
            );
            System.out.println("[OK] Resposta da IA:\n" + resposta);
        } catch (IAException e) {
            System.out.println("[FALHOU] " + e.getMessage());
        }
    }
}