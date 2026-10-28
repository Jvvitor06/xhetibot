package br.com.chatia.service;

import br.com.chatia.model.Documento;
import br.com.chatia.rag.ProcessadorTexto;
import br.com.chatia.rag.ResultadoBusca;
import br.com.chatia.repository.DocumentoRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RAGService {

    private static final int MAX_DOCUMENTOS = 3;
    private static final int MAX_CARACTERES_POR_DOCUMENTO = 1200;
    private static final int TITULO_MAXIMO = 120;

    private final DocumentoRepository documentoRepository = new DocumentoRepository();

    /** Cadastra um documento na base; as palavras-chave são extraídas automaticamente. */
    public Documento cadastrar(String titulo, String conteudo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do documento é obrigatório.");
        }
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("O conteúdo do documento é obrigatório.");
        }
        if (titulo.trim().length() > TITULO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O título pode ter no máximo " + TITULO_MAXIMO + " caracteres."
            );
        }

        Instant agora = Instant.now();
        Documento documento = new Documento();
        documento.setTitulo(titulo.trim());
        documento.setConteudo(conteudo.trim());
        documento.setPalavrasChave(new ArrayList<>(
                ProcessadorTexto.extrairPalavrasRelevantes(titulo + " " + conteudo)
        ));
        documento.setCriadoEm(agora);
        documento.setAtualizadoEm(agora);

        documentoRepository.salvar(documento);
        return documento;
    }

    public boolean existeComTitulo(String titulo) {
        return documentoRepository.existeComTitulo(titulo);
    }

    /** Pergunta -> palavras relevantes -> documentos mais parecidos. */
    public ResultadoBusca buscar(String pergunta) {
        Set<String> palavras = ProcessadorTexto.extrairPalavrasRelevantes(pergunta);
        if (palavras.isEmpty()) {
            return new ResultadoBusca(palavras, List.of());
        }

        List<Documento> candidatos = documentoRepository.buscarPorPalavrasChave(palavras);

        // Quem tem mais palavras em comum vem primeiro
        candidatos.sort(
                Comparator.comparingInt((Documento d) -> pontuacao(d, palavras)).reversed()
                        .thenComparing(Documento::getTitulo)
        );

        List<Documento> melhores = candidatos.size() > MAX_DOCUMENTOS
                ? new ArrayList<>(candidatos.subList(0, MAX_DOCUMENTOS))
                : candidatos;

        return new ResultadoBusca(palavras, melhores);
    }

    /** Monta a instrução enviada à IA, com o contexto encontrado (se houver). */
    public String montarInstrucao(ResultadoBusca busca) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um assistente prestativo. Responda sempre em português do Brasil, ")
                .append("de forma clara e objetiva.\n\n");

        if (!busca.encontrou()) {
            sb.append("Nenhum documento da base de conhecimento foi encontrado para esta ")
                    .append("pergunta. Responda com o seu conhecimento geral.");
            return sb.toString();
        }

        sb.append("Use as informações da BASE DE CONHECIMENTO abaixo quando forem relevantes ")
                .append("para a pergunta. Se a base não tiver a resposta, responda com o seu ")
                .append("conhecimento geral e deixe claro que a informação não veio da base.\n\n")
                .append("=== BASE DE CONHECIMENTO ===\n");

        int numero = 1;
        for (Documento d : busca.documentos()) {
            sb.append("[").append(numero++).append("] ").append(d.getTitulo()).append("\n")
                    .append(limitar(d.getConteudo())).append("\n\n");
        }
        sb.append("=== FIM DA BASE ===");
        return sb.toString();
    }

    private int pontuacao(Documento documento, Set<String> palavras) {
        int pontos = 0;
        for (String chave : documento.getPalavrasChave()) {
            if (palavras.contains(chave)) {
                pontos++;
            }
        }
        return pontos;
    }

    private String limitar(String texto) {
        if (texto.length() <= MAX_CARACTERES_POR_DOCUMENTO) {
            return texto;
        }
        return texto.substring(0, MAX_CARACTERES_POR_DOCUMENTO) + "…";
    }
}