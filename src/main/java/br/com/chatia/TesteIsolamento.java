package br.com.chatia;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Conversa;
import br.com.chatia.model.Mensagem;
import br.com.chatia.model.Usuario;
import br.com.chatia.service.ConversaService;
import br.com.chatia.service.MensagemService;
import br.com.chatia.service.UsuarioService;

import java.util.ArrayList;
import java.util.List;

/**
 * Teste manual de isolamento entre usuários (sem interface gráfica).
 * Rode pelo IntelliJ: botão direito no arquivo -> Run 'TesteIsolamento.main()'.
 */
public class TesteIsolamento {

    private static final String SENHA = "Senha1234";

    // Mude para false se quiser deixar os dados no Atlas para tirar print.
    private static final boolean LIMPAR_NO_FINAL = true;

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) {
        UsuarioService usuarios = new UsuarioService();
        ConversaService conversas = new ConversaService();
        MensagemService mensagens = new MensagemService();

        Usuario u1 = obterUsuario(usuarios, "Usuario Um", "usuario1@teste.com");
        Usuario u2 = obterUsuario(usuarios, "Usuario Dois", "usuario2@teste.com");

        Conversa conversaJava = conversas.criar(u1.getId(), "[teste] Java");
        Conversa conversaFx = conversas.criar(u1.getId(), "[teste] JavaFX");
        Conversa conversaAngular = conversas.criar(u2.getId(), "[teste] Angular");

        System.out.println("\n--- Listagem ---");
        List<String> idsU1 = ids(conversas.listar(u1.getId()));
        List<String> idsU2 = ids(conversas.listar(u2.getId()));

        verificar("usuario1 enxerga suas conversas",
                idsU1.contains(conversaJava.getId()) && idsU1.contains(conversaFx.getId()));
        verificar("usuario1 NÃO enxerga a conversa do usuario2",
                !idsU1.contains(conversaAngular.getId()));
        verificar("usuario2 enxerga sua conversa",
                idsU2.contains(conversaAngular.getId()));
        verificar("usuario2 NÃO enxerga as conversas do usuario1",
                !idsU2.contains(conversaJava.getId()) && !idsU2.contains(conversaFx.getId()));

        System.out.println("\n--- Tentativas de acesso indevido (usuario2 tentando mexer no usuario1) ---");
        verificar("usuario2 não consegue renomear conversa do usuario1",
                falha(() -> conversas.renomear(u2.getId(), conversaJava.getId(), "Invadida")));
        verificar("título da conversa do usuario1 continua intacto",
                conversas.buscar(u1.getId(), conversaJava.getId()).getTitulo().equals("[teste] Java"));
        verificar("usuario2 não consegue excluir conversa do usuario1",
                falha(() -> conversas.excluir(u2.getId(), conversaJava.getId())));
        verificar("conversa do usuario1 continua existindo",
                ids(conversas.listar(u1.getId())).contains(conversaJava.getId()));
        verificar("usuario2 não consegue enviar mensagem na conversa do usuario1",
                falha(() -> mensagens.salvar(u2.getId(), conversaJava.getId(),
                        AutorMensagem.USUARIO, "oi")));
        verificar("usuario2 não consegue ler mensagens do usuario1",
                falha(() -> mensagens.listar(u2.getId(), conversaJava.getId())));

        System.out.println("\n--- Fluxo normal do dono ---");
        mensagens.salvar(u1.getId(), conversaJava.getId(),
                AutorMensagem.USUARIO, "O que é encapsulamento?");
        mensagens.salvar(u1.getId(), conversaJava.getId(),
                AutorMensagem.IA, "É esconder os detalhes internos de uma classe.");
        mensagens.salvar(u2.getId(), conversaAngular.getId(),
                AutorMensagem.USUARIO, "O que é um componente?");

        List<Mensagem> historico = mensagens.listar(u1.getId(), conversaJava.getId());
        verificar("histórico do usuario1 tem 2 mensagens, em ordem",
                historico.size() == 2
                        && historico.get(0).getAutor() == AutorMensagem.USUARIO
                        && historico.get(1).getAutor() == AutorMensagem.IA);

        conversas.renomear(u1.getId(), conversaFx.getId(), "[teste] Estudando JavaFX");
        verificar("dono consegue renomear",
                conversas.buscar(u1.getId(), conversaFx.getId())
                        .getTitulo().equals("[teste] Estudando JavaFX"));

        conversas.excluir(u1.getId(), conversaJava.getId());
        verificar("dono consegue excluir",
                !ids(conversas.listar(u1.getId())).contains(conversaJava.getId()));

        if (LIMPAR_NO_FINAL) {
            tentarExcluir(conversas, u1, conversaFx);
            tentarExcluir(conversas, u2, conversaAngular);
        }

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        MongoConfig.fechar();
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static Usuario obterUsuario(UsuarioService usuarios, String nome, String email) {
        try {
            usuarios.cadastrar(nome, email, SENHA);
        } catch (IllegalArgumentException e) {
            // já existe de uma execução anterior: tudo bem
        }
        Usuario usuario = usuarios.autenticar(email, SENHA);
        if (usuario == null) {
            throw new IllegalStateException("Não foi possível autenticar " + email);
        }
        return usuario;
    }

    private static List<String> ids(List<Conversa> lista) {
        List<String> ids = new ArrayList<>();
        for (Conversa c : lista) {
            ids.add(c.getId());
        }
        return ids;
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

    private static void tentarExcluir(ConversaService conversas, Usuario dono, Conversa conversa) {
        try {
            conversas.excluir(dono.getId(), conversa.getId());
        } catch (IllegalArgumentException e) {
            // já excluída
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