package br.com.chatia.controller;

import br.com.chatia.model.Conversa;
import br.com.chatia.model.Mensagem;
import br.com.chatia.service.ChatService;
import br.com.chatia.service.ConversaService;
import br.com.chatia.service.MensagemService;
import br.com.chatia.util.Sessao;
import br.com.chatia.util.Tarefa;
import br.com.chatia.view.LoginView;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class ChatController {

    private final Stage stage;
    private final ConversaService conversaService = new ConversaService();
    private final MensagemService mensagemService = new MensagemService();
    private final ChatService chatService = new ChatService();

    public ChatController(Stage stage) {
        this.stage = stage;
    }

    public void carregarConversas(Consumer<List<Conversa>> aoSucesso,
                                  Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        Tarefa.executar(() -> conversaService.listar(usuarioId), aoSucesso, aoErro);
    }

    public void criarConversa(Consumer<Conversa> aoSucesso, Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        Tarefa.executar(() -> conversaService.criar(usuarioId, null), aoSucesso, aoErro);
    }

    public void renomear(Conversa conversa, String novoTitulo,
                         Runnable aoSucesso, Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        String conversaId = conversa.getId();
        Tarefa.executar(() -> {
            conversaService.renomear(usuarioId, conversaId, novoTitulo);
            return null;
        }, v -> aoSucesso.run(), aoErro);
    }

    public void excluir(Conversa conversa, Runnable aoSucesso, Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        String conversaId = conversa.getId();
        Tarefa.executar(() -> {
            conversaService.excluir(usuarioId, conversaId);
            return null;
        }, v -> aoSucesso.run(), aoErro);
    }

    public void carregarMensagens(Conversa conversa,
                                  Consumer<List<Mensagem>> aoSucesso,
                                  Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        String conversaId = conversa.getId();
        Tarefa.executar(() -> mensagemService.listar(usuarioId, conversaId),
                aoSucesso, aoErro);
    }

    public void enviar(Conversa conversa, String texto,
                       Consumer<Mensagem> aoSucesso, Consumer<String> aoErro) {
        String usuarioId = usuarioId();
        String conversaId = conversa.getId();
        Tarefa.executar(() -> chatService.enviar(usuarioId, conversaId, texto),
                aoSucesso, aoErro);
    }

    public void sair() {
        Sessao.encerrar();
        new LoginView().show(stage);
    }

    private String usuarioId() {
        return Sessao.getUsuario().getId();
    }
}