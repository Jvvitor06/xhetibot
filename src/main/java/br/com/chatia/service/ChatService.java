package br.com.chatia.service;

import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Conversa;
import br.com.chatia.model.Mensagem;
import br.com.chatia.rag.ResultadoBusca;

import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private static final int HISTORICO_MAXIMO = 20;

    private final MensagemService mensagemService = new MensagemService();
    private final ConversaService conversaService = new ConversaService();
    private final RAGService ragService = new RAGService();
    private final GroqService groqService = new GroqService();

    /**
     * Pergunta -> RAG -> Groq -> resposta.
     * A pergunta e a resposta só são salvas se a IA responder com sucesso,
     * para não deixar perguntas "penduradas" no histórico quando há erro.
     */
    public Mensagem enviar(String usuarioId, String conversaId, String texto) {
        mensagemService.validarConteudo(texto);
        String pergunta = texto.trim();

        // listar() também confirma que a conversa pertence ao usuário
        List<Mensagem> historico =
                new ArrayList<>(mensagemService.listar(usuarioId, conversaId));
        historico.add(novaMensagem(AutorMensagem.USUARIO, pergunta));

        ResultadoBusca busca = buscarContexto(pergunta);
        String instrucao = ragService.montarInstrucao(busca);

        String resposta = groqService.responder(instrucao, ultimas(historico));

        mensagemService.salvar(usuarioId, conversaId, AutorMensagem.USUARIO, pergunta);
        nomearConversaSeNova(usuarioId, conversaId, pergunta);

        return mensagemService.salvar(usuarioId, conversaId, AutorMensagem.IA, resposta);
    }

    /** Se a busca falhar, o chat continua sem contexto em vez de quebrar. */
    private ResultadoBusca buscarContexto(String pergunta) {
        try {
            return ragService.buscar(pergunta);
        } catch (RuntimeException e) {
            System.err.println("[RAG] Busca indisponível, seguindo sem contexto: " + e);
            return ResultadoBusca.vazio();
        }
    }

    /** Só as mensagens mais recentes vão para a IA (economiza tokens). */
    private List<Mensagem> ultimas(List<Mensagem> historico) {
        int inicio = Math.max(0, historico.size() - HISTORICO_MAXIMO);
        return historico.subList(inicio, historico.size());
    }

    private Mensagem novaMensagem(AutorMensagem autor, String conteudo) {
        Mensagem m = new Mensagem();
        m.setAutor(autor);
        m.setConteudo(conteudo);
        return m;
    }

    /** A primeira pergunta vira o título da conversa. */
    private void nomearConversaSeNova(String usuarioId, String conversaId, String texto) {
        Conversa conversa = conversaService.buscar(usuarioId, conversaId);

        if (ConversaService.TITULO_PADRAO.equals(conversa.getTitulo())) {
            String titulo = texto.replaceAll("\\s+", " ");
            if (titulo.length() > 40) {
                titulo = titulo.substring(0, 40).trim() + "…";
            }
            conversaService.renomear(usuarioId, conversaId, titulo);
        }
    }
}