package br.com.chatia;

import br.com.chatia.util.TextoSimples;

/** Testa a limpeza de Markdown (não usa banco nem interface). */
public class TesteTextoSimples {

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) {
        igual("remove #, **, *, crase e troca - por marcador",
                "# Título\n\n**Negrito** e *itálico*\n\n- item um\n- item dois\n\n`código`",
                "Título\n\nNegrito e itálico\n\n\u2022 item um\n\u2022 item dois\n\ncódigo");

        igual("lista com asterisco vira marcador",
                "* primeiro\n* segundo",
                "\u2022 primeiro\n\u2022 segundo");

        igual("títulos de vários níveis",
                "## Subtítulo\n### Outro",
                "Subtítulo\nOutro");

        igual("tabela vira linhas simples",
                "| a | b |\n|---|---|\n| 1 | 2 |",
                "a \u2014 b\n1 \u2014 2");

        igual("link mostra texto e endereço",
                "Veja [o site](https://exemplo.com) agora",
                "Veja o site (https://exemplo.com) agora");

        igual("bloco de código mantém só o conteúdo",
                "```java\nint x = 1;\n```",
                "int x = 1;");

        igual("linha horizontal é removida",
                "antes\n\n---\n\ndepois",
                "antes\n\ndepois");

        igual("lista numerada não muda",
                "1. um\n2. dois",
                "1. um\n2. dois");

        igual("multiplicação não é confundida com itálico",
                "2 * 3 = 6 e 4*5*6",
                "2 * 3 = 6 e 4*5*6");

        igual("snake_case não muda",
                "use meu_metodo_exemplo()",
                "use meu_metodo_exemplo()");

        igual("texto simples não muda",
                "Encapsulamento protege os dados.",
                "Encapsulamento protege os dados.");

        igual("texto nulo vira vazio", null, "");

        String complexo = "# T\n\n**a** *b*\n- x\n\n| c | d |\n|---|---|\n| 1 | 2 |";
        String uma = TextoSimples.limpar(complexo);
        verificar("limpar duas vezes dá o mesmo resultado", uma.equals(TextoSimples.limpar(uma)));

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void igual(String descricao, String entrada, String esperado) {
        String obtido = TextoSimples.limpar(entrada);
        boolean passou = obtido.equals(esperado);
        verificar(descricao, passou);
        if (!passou) {
            System.out.println("         esperado: " + esperado.replace("\n", "\\n"));
            System.out.println("         obtido  : " + obtido.replace("\n", "\\n"));
        }
    }

    private static void verificar(String descricao, boolean passou) {
        if (passou) {
            ok++;
            System.out.println("[OK]     " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHOU] " + descricao);
        }
    }
}