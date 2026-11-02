package br.com.chatia.view;

import br.com.chatia.controller.ChatController;
import br.com.chatia.controller.ThemeController;
import br.com.chatia.model.AutorMensagem;
import br.com.chatia.model.Conversa;
import br.com.chatia.model.Mensagem;
import br.com.chatia.util.Animacoes;
import br.com.chatia.util.Estilos;
import br.com.chatia.util.Sessao;
import br.com.chatia.util.TextoSimples;
import br.com.chatia.util.TransicaoForma;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatView {

    private static final double LARGURA_SIDEBAR = 290;
    private static final double LARGURA_BOLHA = 560;

    private Stage stage;
    private ChatController controller;

    // Componentes usados em vários métodos
    private final ListView<Conversa> listaConversas = new ListView<>();
    private final VBox mensagensBox = new VBox(12);
    private final TextArea campoMensagem = new TextArea();
    private final Button botaoEnviar = new Button("Enviar");
    private final Button botaoNova = new Button("+  Nova conversa");
    private final Label lblTitulo = new Label();
    private final Label lblErro = new Label();
    private final Pane overlay = new Pane();     // camada onde a forma da transição é desenhada
    private ScrollPane scrollMensagens;
    private VBox areaChat;
    private VBox estadoVazio;
    private StackPane raiz;

    // Estado da tela
    private Conversa conversaAtual;
    private boolean atualizandoLista = false;
    private boolean enviando = false;
    private Node ultimaOrigemClique;             // onde o usuário clicou na lista

    public void show(Stage stage) {
        this.stage = stage;
        this.controller = new ChatController(stage);

        BorderPane conteudo = new BorderPane();
        conteudo.getStyleClass().add("chat-root");
        conteudo.setLeft(criarSidebar());
        conteudo.setCenter(criarAreaCentral());

        overlay.setMouseTransparent(true);
        raiz = new StackPane(conteudo, overlay);
        TransicaoForma.registrar(raiz, overlay);

        Scene scene = new Scene(raiz, 1100, 700);
        Estilos.aplicar(scene);

        stage.setTitle("Heaven AI");
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.setScene(scene);
        if (!stage.isMaximized()) {
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();
        }
        stage.show();

        if (TransicaoForma.revelarSeNecessario(overlay)) {
            Animacoes.aparecer(conteudo, 320); // o chat surge enquanto a forma se dissolve
        } else {
            Animacoes.fadeIn(raiz, 450);
        }
        carregarConversasIniciais();
    }

    // ------------------------------------------------------------------
    // Construção da interface
    // ------------------------------------------------------------------

    private VBox criarSidebar() {
        Label marca = new Label("Heaven AI");
        marca.getStyleClass().add("sidebar-titulo");

        botaoNova.getStyleClass().add("botao-primario");
        botaoNova.setMaxWidth(Double.MAX_VALUE);
        botaoNova.setOnAction(e -> novaConversa());

        Label rotulo = new Label("SUAS CONVERSAS");
        rotulo.getStyleClass().add("eyebrow");

        Label vazio = new Label("Nenhuma conversa ainda.");
        vazio.getStyleClass().add("subtitulo");

        listaConversas.getStyleClass().add("lista-conversas");
        listaConversas.setPlaceholder(vazio);
        listaConversas.setCellFactory(lv -> new CelulaConversa());
        listaConversas.getSelectionModel().selectedItemProperty().addListener(
                (obs, antiga, selecionada) -> {
                    if (atualizandoLista || selecionada == null) {
                        return;
                    }
                    Node origem = ultimaOrigemClique != null ? ultimaOrigemClique : listaConversas;
                    ultimaOrigemClique = null;
                    trocarConversa(selecionada, origem);
                }
        );
        VBox.setVgrow(listaConversas, Priority.ALWAYS);

        Label usuario = new Label(Sessao.getUsuario().getNome());
        usuario.getStyleClass().add("sidebar-usuario");

        ThemeController temaController = new ThemeController();
        Button botaoTema = new Button(temaController.textoDoBotao());
        botaoTema.getStyleClass().add("botao-secundario");
        botaoTema.setMaxWidth(Double.MAX_VALUE);
        botaoTema.setOnAction(e -> temaController.alternar(botaoTema));

        Button botaoSair = new Button("Sair");
        botaoSair.getStyleClass().add("botao-secundario");
        botaoSair.setMaxWidth(Double.MAX_VALUE);
        // A tela some aos poucos (fade out) e só depois volta para o login
        botaoSair.setOnAction(e -> Animacoes.fadeOut(raiz, 380, controller::sair));

        VBox sidebar = new VBox(12, marca, botaoNova, rotulo, listaConversas,
                usuario, botaoTema, botaoSair);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(LARGURA_SIDEBAR);
        sidebar.setMinWidth(LARGURA_SIDEBAR);
        sidebar.setMaxWidth(LARGURA_SIDEBAR);
        return sidebar;
    }

    private VBox criarAreaCentral() {
        // Tela exibida quando nenhuma conversa está aberta
        Label tituloVazio = new Label("Nenhuma conversa selecionada");
        tituloVazio.getStyleClass().add("titulo");
        Label textoVazio = new Label("Escolha uma conversa na lateral ou crie uma nova.");
        textoVazio.getStyleClass().add("subtitulo");
        estadoVazio = new VBox(8, tituloVazio, textoVazio);
        estadoVazio.setAlignment(Pos.CENTER);

        // Cabeçalho com o título da conversa
        lblTitulo.getStyleClass().add("chat-titulo");
        HBox cabecalho = new HBox(lblTitulo);
        cabecalho.getStyleClass().add("chat-cabecalho");

        // Lista de mensagens com rolagem
        mensagensBox.setPadding(new Insets(24));
        scrollMensagens = new ScrollPane(mensagensBox);
        scrollMensagens.setFitToWidth(true);
        scrollMensagens.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollMensagens.getStyleClass().add("chat-scroll");
        // Sempre rola até a última mensagem quando o conteúdo cresce
        mensagensBox.heightProperty().addListener(
                (obs, antiga, nova) -> scrollMensagens.setVvalue(1.0)
        );
        VBox.setVgrow(scrollMensagens, Priority.ALWAYS);

        // Campo de digitar + botão enviar
        campoMensagem.setPromptText("Digite sua pergunta...  (Enter envia, Shift+Enter quebra linha)");
        campoMensagem.setWrapText(true);
        campoMensagem.setPrefRowCount(2);
        campoMensagem.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                enviar();
            }
        });
        HBox.setHgrow(campoMensagem, Priority.ALWAYS);

        botaoEnviar.getStyleClass().add("botao-primario");
        botaoEnviar.setOnAction(e -> enviar());

        HBox entrada = new HBox(10, campoMensagem, botaoEnviar);
        entrada.setAlignment(Pos.CENTER);
        entrada.getStyleClass().add("chat-entrada");

        areaChat = new VBox(cabecalho, scrollMensagens, entrada);

        StackPane conteudo = new StackPane(estadoVazio, areaChat);
        VBox.setVgrow(conteudo, Priority.ALWAYS);

        // Faixa de erro (aparece só quando necessário)
        lblErro.getStyleClass().add("banner-erro");
        lblErro.setWrapText(true);
        lblErro.setMaxWidth(Double.MAX_VALUE);
        lblErro.setVisible(false);
        lblErro.setManaged(false);

        mostrarChat(false);

        VBox centro = new VBox(lblErro, conteudo);
        centro.getStyleClass().add("chat-centro");
        return centro;
    }

    /** Cada linha da lista de conversas: título + botão "•••" com o menu. */
    private class CelulaConversa extends ListCell<Conversa> {

        private final Label titulo = new Label();
        private final Button menu = new Button("•••");
        private final HBox linha = new HBox(6);

        CelulaConversa() {
            titulo.getStyleClass().add("conversa-titulo");
            titulo.setMinWidth(0);
            titulo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titulo, Priority.ALWAYS);

            menu.getStyleClass().add("menu-conversa");

            MenuItem renomear = new MenuItem("Renomear");
            renomear.setOnAction(e -> renomearConversa(getItem()));
            MenuItem excluir = new MenuItem("Excluir");
            excluir.setOnAction(e -> excluirConversa(getItem()));
            ContextMenu contexto = new ContextMenu(renomear, excluir);
            menu.setOnAction(e -> contexto.show(menu, Side.BOTTOM, 0, 4));

            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getChildren().addAll(titulo, menu);
            setPrefWidth(0); // evita barra de rolagem horizontal na lista

            // Guarda a célula clicada: é dela que a transição nasce
            setOnMousePressed(e -> ultimaOrigemClique = this);
        }

        @Override
        protected void updateItem(Conversa conversa, boolean vazio) {
            super.updateItem(conversa, vazio);
            if (vazio || conversa == null) {
                setGraphic(null);
            } else {
                titulo.setText(conversa.getTitulo());
                setGraphic(linha);
            }
        }
    }

    // ------------------------------------------------------------------
    // Ações
    // ------------------------------------------------------------------

    private void carregarConversasIniciais() {
        controller.carregarConversas(conversas -> {
            preencherLista(conversas);
            if (!conversas.isEmpty()) {
                atualizandoLista = true; // abertura inicial: sem transição
                try {
                    listaConversas.getSelectionModel().select(0);
                } finally {
                    atualizandoLista = false;
                }
                abrirConversa(conversas.get(0), null);
            }
        }, this::mostrarErro);
    }

    /** O botão muda de forma até virar a tela inteira; por baixo, a nova conversa é criada e aberta. */
    private void novaConversa() {
        limparErro();
        TransicaoForma.executar(botaoNova, retomar ->
                controller.criarConversa(conversa -> {
                    atualizandoLista = true;
                    try {
                        listaConversas.getItems().add(0, conversa);
                        listaConversas.getSelectionModel().select(conversa);
                    } finally {
                        atualizandoLista = false;
                    }
                    conversaAtual = conversa;
                    lblTitulo.setText(conversa.getTitulo());
                    mensagensBox.getChildren().clear();
                    mostrarChat(true);
                    campoMensagem.requestFocus();
                    Animacoes.aparecer(areaChat, 150);
                    retomar.run();
                }, erro -> {
                    mostrarErro(erro);
                    retomar.run();
                })
        );
    }

    /** Mesma transição ao trocar de conversa, saindo do item clicado. */
    private void trocarConversa(Conversa conversa, Node origem) {
        TransicaoForma.executar(origem, retomar -> abrirConversa(conversa, () -> {
            Animacoes.aparecer(areaChat, 150);
            retomar.run();
        }));
    }

    /** Carrega a conversa; "aoTerminar" (pode ser null) revela a tela quando tudo está pronto. */
    private void abrirConversa(Conversa conversa, Runnable aoTerminar) {
        conversaAtual = conversa;
        lblTitulo.setText(conversa.getTitulo());
        limparErro();
        mostrarChat(true);

        mensagensBox.getChildren().setAll(criarEstado("Carregando mensagens..."));

        controller.carregarMensagens(conversa, mensagens -> {
            if (mesmaConversa(conversa)) {
                mensagensBox.getChildren().clear();
                for (Mensagem m : mensagens) {
                    mensagensBox.getChildren().add(
                            criarBolha(m.getAutor(), m.getConteudo(), false, false));
                }
                campoMensagem.requestFocus();
            }
            concluir(aoTerminar);
        }, erro -> {
            if (mesmaConversa(conversa)) {
                mensagensBox.getChildren().clear();
                mostrarErro(erro);
            }
            concluir(aoTerminar);
        });
    }

    private void renomearConversa(Conversa conversa) {
        if (conversa == null) {
            return;
        }

        TextInputDialog dialogo = new TextInputDialog(conversa.getTitulo());
        dialogo.initOwner(stage);
        dialogo.setTitle("Renomear conversa");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Novo título:");
        Estilos.aplicar(dialogo.getDialogPane());

        Optional<String> resposta = dialogo.showAndWait();
        resposta.ifPresent(novoTitulo ->
                controller.renomear(conversa, novoTitulo, this::recarregarLista, this::mostrarErro)
        );
    }

    private void excluirConversa(Conversa conversa) {
        if (conversa == null) {
            return;
        }

        ButtonType excluir = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Excluir \"" + conversa.getTitulo() + "\"?\n"
                        + "As mensagens também serão apagadas. Esta ação não pode ser desfeita.",
                excluir, cancelar
        );
        alerta.initOwner(stage);
        alerta.setTitle("Excluir conversa");
        alerta.setHeaderText(null);
        Estilos.aplicar(alerta.getDialogPane());

        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isEmpty() || resposta.get() != excluir) {
            return;
        }

        controller.excluir(conversa, () -> {
            if (mesmaConversa(conversa)) {
                conversaAtual = null;
                mensagensBox.getChildren().clear();
                mostrarChat(false);
            }
            recarregarLista();
        }, this::mostrarErro);
    }

    private void enviar() {
        if (conversaAtual == null || enviando) {
            return;
        }
        String texto = campoMensagem.getText().trim();
        if (texto.isEmpty()) {
            return;
        }

        Conversa conversa = conversaAtual;
        limparErro();
        campoMensagem.clear();
        definirEnviando(true);

        HBox bolhaUsuario = criarBolha(AutorMensagem.USUARIO, texto, true, false);
        HBox indicador = criarIndicadorDigitando();
        mensagensBox.getChildren().addAll(bolhaUsuario, indicador);

        controller.enviar(conversa, texto, resposta -> {
            definirEnviando(false);
            removerIndicador(indicador);
            if (mesmaConversa(conversa)) {
                // A resposta da IA entra e é "digitada" na tela
                mensagensBox.getChildren().add(
                        criarBolha(AutorMensagem.IA, resposta.getConteudo(), true, true));
            }
            recarregarLista(); // atualiza título automático e ordem
        }, erro -> {
            definirEnviando(false);
            removerIndicador(indicador);
            if (mesmaConversa(conversa)) {
                // A pergunta não foi salva: tira o balão e devolve o texto ao campo
                mensagensBox.getChildren().remove(bolhaUsuario);
                mostrarErro(erro);
                campoMensagem.setText(texto);
            }
            recarregarLista();
        });
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void concluir(Runnable acao) {
        if (acao != null) {
            acao.run();
        }
    }

    private void recarregarLista() {
        controller.carregarConversas(this::preencherLista, this::mostrarErro);
    }

    /** Troca os itens da lista mantendo a conversa atual selecionada. */
    private void preencherLista(List<Conversa> conversas) {
        atualizandoLista = true; // evita reabrir a conversa por causa da troca de itens
        try {
            listaConversas.getItems().setAll(conversas);

            if (conversaAtual != null) {
                for (int i = 0; i < conversas.size(); i++) {
                    if (conversas.get(i).getId().equals(conversaAtual.getId())) {
                        conversaAtual = conversas.get(i);
                        lblTitulo.setText(conversaAtual.getTitulo());
                        listaConversas.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } finally {
            atualizandoLista = false;
        }
    }

    private boolean mesmaConversa(Conversa conversa) {
        return conversaAtual != null && conversaAtual.getId().equals(conversa.getId());
    }

    private void mostrarChat(boolean mostrar) {
        areaChat.setVisible(mostrar);
        areaChat.setManaged(mostrar);
        estadoVazio.setVisible(!mostrar);
        estadoVazio.setManaged(!mostrar);
    }

    private void definirEnviando(boolean valor) {
        enviando = valor;
        botaoEnviar.setDisable(valor);
        botaoEnviar.setText(valor ? "Enviando..." : "Enviar");
    }

    /**
     * @param animar  a bolha entra deslizando
     * @param digitar o texto aparece aos poucos (só para respostas novas da IA)
     */
    private HBox criarBolha(AutorMensagem autor, String texto, boolean animar, boolean digitar) {
        boolean doUsuario = autor == AutorMensagem.USUARIO;
        // Respostas da IA passam pelo limpador de Markdown (#, **, - ...)
        String exibido = doUsuario ? texto : TextoSimples.limpar(texto);

        Label conteudo = new Label(digitar ? "" : exibido);
        conteudo.setWrapText(true);
        conteudo.setMaxWidth(LARGURA_BOLHA);
        conteudo.getStyleClass().add(doUsuario ? "bolha-usuario" : "bolha-ia");

        HBox linha = new HBox(conteudo);
        linha.setAlignment(doUsuario ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        if (animar) {
            Animacoes.entrarBolha(linha, doUsuario);
        }
        if (digitar) {
            Animacoes.digitar(conteudo, exibido);
        }
        return linha;
    }

    /** Balão da IA com três pontinhos pulando enquanto a resposta não chega. */
    private HBox criarIndicadorDigitando() {
        List<Circle> pontos = new ArrayList<>();
        HBox balao = new HBox(6);
        balao.setAlignment(Pos.CENTER);
        balao.getStyleClass().add("bolha-ia");
        balao.setMaxWidth(Region.USE_PREF_SIZE);

        for (int i = 0; i < 3; i++) {
            Circle ponto = new Circle(4);
            ponto.getStyleClass().add("ponto-digitando");
            pontos.add(ponto);
            balao.getChildren().add(ponto);
        }

        HBox linha = new HBox(balao);
        linha.setAlignment(Pos.CENTER_LEFT);
        Animacoes.entrarBolha(linha, false);
        Animacoes.guardar(linha, Animacoes.animarPontos(pontos));
        return linha;
    }

    private void removerIndicador(HBox indicador) {
        Animacoes.parar(indicador);
        mensagensBox.getChildren().remove(indicador);
    }

    private Label criarEstado(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("subtitulo");
        return label;
    }

    private void mostrarErro(String texto) {
        Estilos.erro(lblErro, texto);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void limparErro() {
        lblErro.setText("");
        lblErro.setVisible(false);
        lblErro.setManaged(false);
    }
}