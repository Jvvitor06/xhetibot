package br.com.chatia.service;

import br.com.chatia.config.AppConfig;
import br.com.chatia.exception.IAException;
import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Mensagem;
import org.bson.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Conversa com a API da Groq (formato compatível com OpenAI). */
public class GroqService {

    private static final URI ENDPOINT =
            URI.create("https://api.groq.com/openai/v1/chat/completions");

    // Os modelos Llama 3.x foram desativados em 16/08/2026.
    // Para trocar de modelo sem mexer no código: GROQ_MODEL no config.properties.
    private static final String MODELO_PADRAO = "openai/gpt-oss-120b";

    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(10);
    private static final Duration TIMEOUT_RESPOSTA = Duration.ofSeconds(60);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT_CONEXAO)
            .build();

    public String responder(String instrucaoSistema, List<Mensagem> historico) {
        String chave = AppConfig.get("GROQ_API_KEY");
        if (chave == null) {
            throw new IAException(
                    "A chave da IA (GROQ_API_KEY) não está configurada. "
                            + "Consulte o README para configurá-la."
            );
        }

        String modelo = AppConfig.get("GROQ_MODEL");
        if (modelo == null) {
            modelo = MODELO_PADRAO;
        }

        HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT)
                .timeout(TIMEOUT_RESPOSTA)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + chave)
                .POST(HttpRequest.BodyPublishers.ofString(
                        montarCorpo(modelo, instrucaoSistema, historico),
                        StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resposta;
        try {
            resposta = http.send(requisicao,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpConnectTimeoutException e) {
            throw new IAException(
                    "Não foi possível conectar à IA. Verifique sua conexão com a internet.", e);
        } catch (HttpTimeoutException e) {
            throw new IAException("A IA demorou demais para responder. Tente novamente.", e);
        } catch (IOException e) {
            throw new IAException(
                    "Não foi possível conectar à IA. Verifique sua conexão com a internet.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IAException("A operação foi interrompida.", e);
        }

        if (resposta.statusCode() == 200) {
            return extrairResposta(resposta.body());
        }
        throw erroHttp(resposta.statusCode(), resposta.body());
    }

    private String montarCorpo(String modelo, String instrucao, List<Mensagem> historico) {
        List<Document> mensagens = new ArrayList<>();
        mensagens.add(new Document("role", "system").append("content", instrucao));

        for (Mensagem m : historico) {
            String papel = m.getAutor() == AutorMensagem.USUARIO ? "user" : "assistant";
            mensagens.add(new Document("role", papel).append("content", m.getConteudo()));
        }

        return new Document("model", modelo)
                .append("messages", mensagens)
                .append("temperature", 0.3)
                .toJson();
    }

    private String extrairResposta(String corpo) {
        try {
            Document json = Document.parse(corpo);
            List<Document> escolhas = json.getList("choices", Document.class);

            if (escolhas != null && !escolhas.isEmpty()) {
                Document mensagem = escolhas.get(0).get("message", Document.class);
                if (mensagem != null) {
                    String conteudo = mensagem.getString("content");
                    if (conteudo != null && !conteudo.isBlank()) {
                        return conteudo.trim();
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new IAException("A resposta da IA veio em um formato inesperado.", e);
        }
        throw new IAException("A IA devolveu uma resposta vazia. Tente novamente.");
    }

    private IAException erroHttp(int status, String corpo) {
        // Detalhe técnico só no console (o corpo do erro não contém a chave).
        System.err.println("[Groq] HTTP " + status + " -> " + corpo);

        return switch (status) {
            case 401, 403 -> new IAException(
                    "A chave da IA (GROQ_API_KEY) é inválida ou não tem permissão.");
            case 429 -> new IAException(
                    "O limite de uso da IA foi atingido. Aguarde um pouco e tente de novo.");
            case 400, 404 -> new IAException(
                    "A IA recusou a requisição. Se persistir, confira o modelo em GROQ_MODEL.");
            default -> status >= 500
                    ? new IAException("O serviço de IA está indisponível no momento. Tente em instantes.")
                    : new IAException("A IA retornou um erro inesperado (código " + status + ").");
        };
    }
}