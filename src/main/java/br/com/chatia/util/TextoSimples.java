package br.com.chatia.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remove a formatação Markdown que a IA costuma devolver (#, **, *, -, crases,
 * tabelas...), deixando só texto limpo. É idempotente: limpar duas vezes dá o mesmo.
 */
public final class TextoSimples {

    private static final String MARCADOR = "\u2022"; // •

    private static final Pattern CERCA_CODIGO = Pattern.compile("(?m)^[ \\t]*```.*$\\n?");
    private static final Pattern LINHA_HORIZONTAL =
            Pattern.compile("(?m)^[ \\t]*([-*_])([ \\t]*\\1){2,}[ \\t]*$\\n?");
    private static final Pattern TABELA_SEPARADOR = Pattern.compile(
            "(?m)^[ \\t]*\\|?[ \\t]*:?-{2,}:?[ \\t]*(\\|[ \\t]*:?-{2,}:?[ \\t]*)*\\|?[ \\t]*$\\n?");
    private static final Pattern TABELA_LINHA = Pattern.compile("(?m)^[ \\t]*\\|(.+)\\|[ \\t]*$");
    private static final Pattern TITULO = Pattern.compile("(?m)^[ \\t]{0,3}#{1,6}[ \\t]*");
    private static final Pattern CITACAO = Pattern.compile("(?m)^[ \\t]*>[ \\t]?");
    private static final Pattern LISTA = Pattern.compile("(?m)^([ \\t]*)[-*+][ \\t]+");
    private static final Pattern NEGRITO = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALICO =
            Pattern.compile("(?<![\\w*])\\*(?!\\s)([^*\\n]+?)(?<!\\s)\\*(?![\\w*])");
    private static final Pattern CODIGO_INLINE = Pattern.compile("`([^`\\n]*)`");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]\\n]+)]\\(([^)\\s]+)\\)");
    private static final Pattern ESPACOS_FINAIS = Pattern.compile("(?m)[ \\t]+$");
    private static final Pattern LINHAS_VAZIAS = Pattern.compile("\\n{3,}");

    private TextoSimples() {
    }

    public static String limpar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        String s = texto.replace("\r\n", "\n").replace('\r', '\n');

        s = CERCA_CODIGO.matcher(s).replaceAll("");
        s = LINHA_HORIZONTAL.matcher(s).replaceAll("");
        s = TABELA_SEPARADOR.matcher(s).replaceAll("");
        s = converterLinhasDeTabela(s);
        s = TITULO.matcher(s).replaceAll("");
        s = CITACAO.matcher(s).replaceAll("");
        s = LISTA.matcher(s).replaceAll("$1" + MARCADOR + " ");
        s = NEGRITO.matcher(s).replaceAll("$1");
        s = ITALICO.matcher(s).replaceAll("$1");
        s = CODIGO_INLINE.matcher(s).replaceAll("$1");
        s = LINK.matcher(s).replaceAll("$1 ($2)");
        s = s.replace("**", "");   // sobras de negrito sem par
        s = ESPACOS_FINAIS.matcher(s).replaceAll("");
        s = LINHAS_VAZIAS.matcher(s).replaceAll("\n\n");

        return s.trim();
    }

    /** "| a | b |" vira "a — b". */
    private static String converterLinhasDeTabela(String texto) {
        Matcher m = TABELA_LINHA.matcher(texto);
        StringBuilder resultado = new StringBuilder();
        while (m.find()) {
            String[] celulas = m.group(1).split("\\|");
            StringBuilder linha = new StringBuilder();
            for (String celula : celulas) {
                if (linha.length() > 0) {
                    linha.append(" \u2014 ");
                }
                linha.append(celula.trim());
            }
            m.appendReplacement(resultado, Matcher.quoteReplacement(linha.toString()));
        }
        m.appendTail(resultado);
        return resultado.toString();
    }
}