package br.com.chatia.rag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Palavras muito comuns que não ajudam a identificar o assunto de uma pergunta.
 * Já estão escritas sem acento e em minúsculas, no mesmo formato do texto normalizado.
 */
public final class StopWords {

    private static final Set<String> PALAVRAS = new HashSet<>(Arrays.asList(
            // artigos
            "a", "o", "as", "os", "um", "uma", "uns", "umas",
            // preposições e contrações
            "de", "da", "do", "das", "dos", "em", "no", "na", "nos", "nas",
            "para", "por", "pra", "com", "sem", "sobre", "entre", "ate", "ao", "aos",
            // interrogativos e conjunções
            "que", "qual", "quais", "quem", "como", "quando", "onde", "porque",
            "e", "ou", "mas", "se", "nem",
            // advérbios e quantificadores
            "tambem", "so", "ja", "mais", "menos", "muito", "muita", "pouco",
            // pronomes
            "eu", "tu", "ele", "ela", "eles", "elas", "voce", "voces", "me", "te", "lhe",
            "meu", "minha", "seu", "sua", "este", "esta", "isto", "esse", "essa", "isso",
            "aquele", "aquela", "aquilo",
            // verbos muito comuns
            "ser", "sao", "era", "foi", "sera", "estao", "estou", "ter", "tem", "tenho",
            "ha", "pode", "podem", "posso", "deve", "devo",
            // negação e afirmação
            "nao", "sim",
            // palavras de pedido ("explique X", "quero saber X")
            "explique", "explica", "diga", "fale", "mostre", "conte", "favor",
            "gostaria", "quero", "preciso"
    ));

    private StopWords() {
    }

    public static boolean contem(String palavra) {
        return PALAVRAS.contains(palavra);
    }

    public static Set<String> todas() {
        return Collections.unmodifiableSet(PALAVRAS);
    }
}