package br.com.chatia.rag;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pipeline de processamento de texto do RAG:
 * normalização -> tokenização -> remoção de stop words -> palavras relevantes.
 */
public final class ProcessadorTexto {

    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}+");
    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    private ProcessadorTexto() {
    }

    /** "O que é Encapsulamento?" -> "o que e encapsulamento" */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String decomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String semAcento = MARCAS_DE_ACENTO.matcher(decomposto).replaceAll("");
        String minusculo = semAcento.toLowerCase(Locale.ROOT);
        return NAO_ALFANUMERICO.matcher(minusculo).replaceAll(" ").trim();
    }

    /** "o que e encapsulamento" -> [o, que, e, encapsulamento] */
    public static List<String> tokenizar(String textoNormalizado) {
        if (textoNormalizado == null || textoNormalizado.isBlank()) {
            return List.of();
        }
        return Arrays.asList(textoNormalizado.split(" "));
    }

    /** Remove stop words e palavras de 1 letra. */
    public static List<String> removerStopWords(List<String> tokens) {
        return tokens.stream()
                .filter(t -> t.length() >= 2)
                .filter(t -> !StopWords.contem(t))
                .toList();
    }

    /** Executa o pipeline completo e devolve palavras únicas, na ordem em que apareceram. */
    public static Set<String> extrairPalavrasRelevantes(String texto) {
        Set<String> relevantes = new LinkedHashSet<>();
        for (String token : removerStopWords(tokenizar(normalizar(texto)))) {
            relevantes.add(removerPlural(token));
        }
        return relevantes;
    }

    /**
     * Plural simples: "classes" -> "classe". Não é um stemmer completo,
     * mas faz "senha" e "senhas" serem tratadas como a mesma palavra.
     */
    private static String removerPlural(String palavra) {
        if (palavra.length() > 3 && palavra.endsWith("s") && !palavra.endsWith("ss")) {
            return palavra.substring(0, palavra.length() - 1);
        }
        return palavra;
    }
}