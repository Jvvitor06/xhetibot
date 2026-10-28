package br.com.chatia.service;

import br.com.chatia.model.TokenRecuperacao;
import br.com.chatia.model.Usuario;
import br.com.chatia.repository.PasswordResetRepository;
import br.com.chatia.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

public class PasswordRecoveryService {

    public static final int VALIDADE_MINUTOS = 15;

    private static final int MAX_TENTATIVAS = 5;
    private static final int TAMANHO_CODIGO = 8;
    // Sem 0, 1, I e O, que se confundem ao ler
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String CODIGO_INVALIDO =
            "Código inválido ou expirado. Solicite um novo código.";

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PasswordResetRepository resetRepository = new PasswordResetRepository();
    private final UsuarioService usuarioService = new UsuarioService();
    private final SecureRandom random = new SecureRandom();

    /**
     * Gera um código temporário. Devolve o código (formato ABCD-EFGH) se a conta
     * existe, ou null se não existe. Em produção, o código seria ENVIADO por e-mail
     * em vez de devolvido; aqui ele é mostrado na tela (modo demonstração).
     */
    public String solicitar(String email) {
        return solicitar(email, Duration.ofMinutes(VALIDADE_MINUTOS));
    }

    /** Versão com validade configurável (usada nos testes). */
    public String solicitar(String email, Duration validade) {
        String emailLimpo = normalizarEmail(email);

        Usuario usuario = usuarioRepository.buscarPorEmail(emailLimpo);
        if (usuario == null) {
            return null;
        }

        String codigo = gerarCodigo();
        resetRepository.substituir(
                usuario.getId(),
                hash(codigo),
                Instant.now().plus(validade)
        );

        return codigo.substring(0, 4) + "-" + codigo.substring(4);
    }

    public void redefinirSenha(String email, String codigoDigitado, String novaSenha) {
        String emailLimpo = normalizarEmail(email);

        if (codigoDigitado == null || codigoDigitado.isBlank()) {
            throw new IllegalArgumentException("Informe o código de recuperação.");
        }

        // Senha inválida é recusada ANTES de mexer no código (não gasta tentativa)
        usuarioService.validarSenha(novaSenha);

        Usuario usuario = usuarioRepository.buscarPorEmail(emailLimpo);
        if (usuario == null) {
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }

        TokenRecuperacao token = resetRepository.buscarPorUsuario(usuario.getId());

        if (token == null || token.getExpiraEm().isBefore(Instant.now())) {
            resetRepository.excluirDoUsuario(usuario.getId());
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }

        if (token.getTentativas() >= MAX_TENTATIVAS) {
            resetRepository.excluirDoUsuario(usuario.getId());
            throw new IllegalArgumentException(
                    "Muitas tentativas incorretas. Solicite um novo código.");
        }

        if (!hashConfere(codigoDigitado, token.getTokenHash())) {
            resetRepository.incrementarTentativas(usuario.getId());
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }

        // Código correto: troca a senha e só então consome o código
        usuarioService.redefinirSenha(usuario.getId(), novaSenha);
        resetRepository.excluirDoUsuario(usuario.getId());
    }

    // ------------------------------------------------------------------

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail da sua conta.");
        }
        String limpo = email.trim().toLowerCase(Locale.ROOT);
        if (!limpo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Digite um e-mail válido.");
        }
        return limpo;
    }

    private String gerarCodigo() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    /** "k7qm-4xpa" e "K7QM4XPA" são o mesmo código. */
    private String limpar(String codigo) {
        return codigo.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String hash(String codigo) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest(limpar(codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    /** Comparação em tempo constante (não vaza onde os textos diferem). */
    private boolean hashConfere(String codigoDigitado, String hashEsperado) {
        return MessageDigest.isEqual(
                hash(codigoDigitado).getBytes(StandardCharsets.UTF_8),
                hashEsperado.getBytes(StandardCharsets.UTF_8)
        );
    }
}