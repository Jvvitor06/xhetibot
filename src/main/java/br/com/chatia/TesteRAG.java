package br.com.chatia;

import br.com.chatia.config.MongoConfig;
import br.com.chatia.model.Documento;
import br.com.chatia.rag.ProcessadorTexto;
import br.com.chatia.rag.ResultadoBusca;
import br.com.chatia.rag.StopWords;
import br.com.chatia.service.RAGService;

import java.util.List;
import java.util.Set;

/**
 * Teste manual do RAG. Cadastra uma base de exemplo (se ainda não existir)
 * e verifica stop words, palavra existente e palavra inexistente.
 */
public class TesteRAG {

    private static int ok = 0;
    private static int falhas = 0;

    private static final String[][] BASE_EXEMPLO = {
            {"Encapsulamento em Java",
                    "Encapsulamento é um dos pilares da programação orientada a objetos. "
                            + "Consiste em esconder os atributos de uma classe, declarando-os como private, "
                            + "e permitir o acesso apenas por meio de métodos públicos, como getters e setters. "
                            + "Assim a classe controla como seus dados são lidos e alterados, "
                            + "o que protege a integridade do objeto."},
            {"Arquitetura MVC",
                    "MVC separa a aplicação em três responsabilidades: Model, que representa os dados "
                            + "e as regras do domínio; View, que é a interface exibida ao usuário; e Controller, "
                            + "que recebe as ações da interface e as encaminha. Este projeto tem ainda as camadas "
                            + "Service, com as regras de negócio, e Repository, que conversa com o banco MongoDB."},
            {"JavaFX e CSS",
                    "JavaFX é a biblioteca usada para criar interfaces gráficas desktop em Java. "
                            + "A aparência dos componentes pode ser controlada com JavaFX CSS, usando propriedades "
                            + "que começam com -fx-. Para trocar de tema basta substituir a folha de estilo da Scene, "
                            + "por exemplo entre light.css e dark.css."},
            {"MongoDB Atlas",
                    "MongoDB Atlas é o serviço de banco de dados na nuvem do MongoDB. Ele guarda os dados "
                            + "em documentos no formato BSON, parecido com JSON, organizados em coleções. "
                            + "As coleções principais deste projeto são usuarios, conversas, mensagens e documents."},
            {"Armazenamento seguro de senhas",
                    "Senhas nunca devem ser guardadas em texto puro. O projeto usa o algoritmo BCrypt, "
                            + "que gera um hash com sal aleatório e é propositalmente lento, o que dificulta "
                            + "ataques de força bruta. No login, a senha digitada é comparada com o hash salvo no banco."},
            {"O que é RAG",
                    "RAG significa geração aumentada por recuperação. Antes de perguntar à inteligência "
                            + "artificial, o sistema procura em uma base de conhecimento os documentos relacionados "
                            + "às palavras-chave da pergunta e envia esse contexto junto com ela. Assim a resposta "
                            + "se baseia em informações conhecidas, e não apenas no que o modelo aprendeu no treinamento."}
    };

    public static void main(String[] args) {
        RAGService rag = new RAGService();

        System.out.println("--- Pipeline de texto ---");
        String pergunta = "O que é encapsulamento em Java?";
        String normalizado = ProcessadorTexto.normalizar(pergunta);
        List<String> tokens = ProcessadorTexto.tokenizar(normalizado);
        List<String> semStop = ProcessadorTexto.removerStopWords(tokens);
        Set<String> relevantes = ProcessadorTexto.extrairPalavrasRelevantes(pergunta);

        System.out.println("Original       : " + pergunta);
        System.out.println("Normalizado    : " + normalizado);
        System.out.println("Tokens         : " + tokens);
        System.out.println("Sem stop words : " + semStop);
        System.out.println("Relevantes     : " + relevantes);

        verificar("stop words (o, que, e, em) foram removidas",
                tokens.containsAll(List.of("o", "que", "e", "em"))
                        && semStop.stream().noneMatch(StopWords::contem));
        verificar("sobram só as palavras relevantes",
                semStop.equals(List.of("encapsulamento", "java")));
        verificar("pergunta só com stop words não gera palavras",
                ProcessadorTexto.extrairPalavrasRelevantes("O que é isso?").isEmpty());

        System.out.println("\n--- Cadastro de documentos ---");
        int novos = 0;
        for (String[] doc : BASE_EXEMPLO) {
            if (!rag.existeComTitulo(doc[0])) {
                rag.cadastrar(doc[0], doc[1]);
                novos++;
            }
        }
        System.out.println(novos + " documento(s) novo(s) cadastrado(s).");
        verificar("cadastro sem título é recusado",
                falha(() -> rag.cadastrar("   ", "conteúdo")));
        verificar("cadastro sem conteúdo é recusado",
                falha(() -> rag.cadastrar("Título", "")));

        System.out.println("\n--- Busca por palavra-chave existente ---");
        verificar("'encapsulamento' acha o documento certo em 1º lugar",
                primeiro(rag, "Explique encapsulamento em Java").equals("Encapsulamento em Java"));
        verificar("'BCrypt' acha o documento de senhas",
                primeiro(rag, "Como funciona o BCrypt?").equals("Armazenamento seguro de senhas"));
        verificar("'MVC' acha o documento de arquitetura",
                primeiro(rag, "O que é MVC?").equals("Arquitetura MVC"));
        verificar("plural também acha ('senhas' -> 'senha')",
                primeiro(rag, "Como guardar senhas?").equals("Armazenamento seguro de senhas"));

        System.out.println("\n--- Busca por palavra-chave inexistente ---");
        ResultadoBusca vazio = rag.buscar("Qual a capital da Mongólia?");
        System.out.println("Palavras usadas: " + vazio.palavras());
        verificar("pergunta fora da base não encontra documento", !vazio.encontrou());

        System.out.println("\n--- Contexto enviado à IA ---");
        String instrucao = rag.montarInstrucao(rag.buscar("O que é encapsulamento?"));
        verificar("a instrução inclui o texto do documento encontrado",
                instrucao.contains("Encapsulamento em Java") && instrucao.contains("BASE DE CONHECIMENTO"));
        verificar("sem documento, a instrução avisa que não há contexto",
                rag.montarInstrucao(vazio).contains("Nenhum documento"));

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        MongoConfig.fechar();
        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static String primeiro(RAGService rag, String pergunta) {
        ResultadoBusca busca = rag.buscar(pergunta);
        System.out.println("Pergunta: \"" + pergunta + "\"");
        System.out.println("  palavras : " + busca.palavras());
        System.out.print("  achou    : ");
        if (!busca.encontrou()) {
            System.out.println("(nada)");
            return "";
        }
        for (Documento d : busca.documentos()) {
            System.out.print("[" + d.getTitulo() + "] ");
        }
        System.out.println();
        return busca.documentos().get(0).getTitulo();
    }

    private static boolean falha(Runnable acao) {
        try {
            acao.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
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