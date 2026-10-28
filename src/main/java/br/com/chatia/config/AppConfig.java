package br.com.chatia.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lê configurações sensíveis fora do código-fonte.
 * Ordem de busca:
 *   1) variável de ambiente
 *   2) arquivo  <pasta do usuário>/.chatia/config.properties
 * O arquivo fica fora do projeto, então nunca vai para o Git.
 */
public final class AppConfig {

    private static final Path ARQUIVO = Path.of(
            System.getProperty("user.home"), ".chatia", "config.properties"
    );

    private AppConfig() {
    }

    public static String get(String chave) {
        String valor = System.getenv(chave);
        if (valor != null && !valor.isBlank()) {
            return valor.trim();
        }

        valor = lerDoArquivo(chave);
        if (valor != null && !valor.isBlank()) {
            return valor.trim();
        }

        return null;
    }

    public static Path getCaminhoArquivo() {
        return ARQUIVO;
    }

    private static String lerDoArquivo(String chave) {
        if (!Files.isRegularFile(ARQUIVO)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(ARQUIVO)) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty(chave);
        } catch (IOException e) {
            return null;
        }
    }
}
