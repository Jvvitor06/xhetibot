package br.com.chatia;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.service.PasswordRecoveryService;
import br.com.chatia.service.UsuarioService;

import java.time.Duration;

/** Teste manual da recuperação de senha (sem interface). */
public class TesteRecuperacaoSenha {

    private static final String EMAIL = "recuperacao@teste.com";
    private static final String SENHA_ANTIGA = "SenhaAntiga1";
    private static final String SENHA_NOVA = "SenhaNova123";

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws Exception {
        UsuarioService usuarios = new UsuarioService();
        PasswordRecoveryService recuperacao = new PasswordRecoveryService();

        // Prepara: garante a conta e uma senha conhecida (o teste pode ser repetido)
        try {
            usuarios.cadastrar("Usuario Recuperacao", EMAIL, SENHA_ANTIGA);
        } catch (IllegalArgumentException e) {
            // já existe
        }
        recuperacao.redefinirSenha(EMAIL, recuperacao.solicitar(EMAIL), SENHA_ANTIGA);

        System.out.println("--- Geração do código ---");
        String codigo = recuperacao.solicitar(EMAIL);
        System.out.println("Código gerado: " + codigo);
        verificar("login com a senha antiga funciona",
                usuarios.autenticar(EMAIL, SENHA_ANTIGA) != null);
        verificar("código tem o formato XXXX-XXXX",
                codigo != null && codigo.matches("[A-Z2-9]{4}-[A-Z2-9]{4}"));
        verificar("e-mail inexistente não gera código (e não dá erro)",
                recuperacao.solicitar("naoexiste@teste.com") == null);
        verificar("e-mail inválido é recusado",
                falha(() -> recuperacao.solicitar("abc")));

        System.out.println("\n--- Validações ---");
        verificar("código errado é recusado",
                falha(() -> recuperacao.redefinirSenha(EMAIL, "AAAA-AAAA", SENHA_NOVA)));
        final String codigoAtual = recuperacao.solicitar(EMAIL);
        verificar("senha curta é recusada",
                falha(() -> recuperacao.redefinirSenha(EMAIL, codigoAtual, "123")));
        verificar("senha curta NÃO consumiu o código",
                executa(() -> recuperacao.redefinirSenha(EMAIL, codigoAtual, SENHA_NOVA)));

        System.out.println("\n--- Resultado da troca ---");
        verificar("login com a senha NOVA funciona",
                usuarios.autenticar(EMAIL, SENHA_NOVA) != null);
        verificar("a senha ANTIGA deixou de funcionar",
                usuarios.autenticar(EMAIL, SENHA_ANTIGA) == null);
        verificar("o código não pode ser usado duas vezes",
                falha(() -> recuperacao.redefinirSenha(EMAIL, codigoAtual, "OutraSenha123")));

        System.out.println("\n--- Expiração e limite de tentativas ---");
        final String expirado = recuperacao.solicitar(EMAIL, Duration.ofMillis(1));
        Thread.sleep(100);
        verificar("código expirado é recusado",
                falha(() -> recuperacao.redefinirSenha(EMAIL, expirado, SENHA_ANTIGA)));

        final String bloqueado = recuperacao.solicitar(EMAIL);
        boolean todasRecusadas = true;
        for (int i = 0; i < 5; i++) {
            todasRecusadas &= falha(() ->
                    recuperacao.redefinirSenha(EMAIL, "AAAA-AAAA", SENHA_ANTIGA));
        }
        verificar("5 tentativas erradas são recusadas", todasRecusadas);
        verificar("depois de 5 erros, nem o código certo funciona",
                falha(() -> recuperacao.redefinirSenha(EMAIL, bloqueado, SENHA_ANTIGA)));

        // Restaura a senha antiga para o teste poder rodar de novo
        recuperacao.redefinirSenha(EMAIL, recuperacao.solicitar(EMAIL), SENHA_ANTIGA);
        verificar("senha restaurada ao final do teste",
                usuarios.autenticar(EMAIL, SENHA_ANTIGA) != null);

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        MongoConfig.fechar();
        if (falhas > 0) {
            System.exit(1);
        }
    }

    /** true se a ação foi recusada com IllegalArgumentException (o esperado). */
    private static boolean falha(Runnable acao) {
        try {
            acao.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /** true se a ação terminou sem erro. */
    private static boolean executa(Runnable acao) {
        try {
            acao.run();
            return true;
        } catch (RuntimeException e) {
            return false;
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