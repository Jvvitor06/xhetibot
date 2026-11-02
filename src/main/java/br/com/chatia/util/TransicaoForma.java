package br.com.chatia.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * O botão clicado se expande até ocupar a janela inteira e a nova tela se abre ali.
 *
 * É UMA forma só: um retângulo arredondado que nasce exatamente sobre o botão e muda
 * de posição, tamanho, cantos e cor (do azul do botão para a cor de fundo da tela
 * nova). Quando ocupa a janela, a nova tela é preparada por baixo; em seguida a forma
 * se dissolve e o conteúdo sobe e aparece.
 *
 *  - executar(): a tela continua a mesma (ex.: nova conversa, trocar de conversa);
 *  - navegar():  a tela inteira é trocada por outra (ex.: login -> chat). A nova tela
 *                chama revelarSeNecessario() para continuar a animação.
 */
public final class TransicaoForma {

    private static final String OCUPADO = "transicao-ocupada";
    private static final String CHAVE_OVERLAY = "overlay-transicao";

    private static final Color COR_BOTAO = Color.web("#1088dd");
    private static final double ARCO_BOTAO = 20;        // cantos arredondados do botão
    private static final double DURACAO_EXPANDIR = 620;
    private static final double DURACAO_DISSOLVER = 380;
    private static final Interpolator CURVA = Interpolator.SPLINE(0.7, 0, 0.2, 1);
    private static final long VALIDADE_NS = 2_000_000_000L;

    private static boolean revelarProximaTela = false;
    private static long marcaRevelar = 0;

    private TransicaoForma() {
    }

    /** Cada tela registra o painel transparente (overlay) onde a forma é desenhada. */
    public static void registrar(Parent raiz, Pane overlay) {
        raiz.getProperties().put(CHAVE_OVERLAY, overlay);
    }

    // ------------------------------------------------------------------
    // Mesma tela
    // ------------------------------------------------------------------

    /**
     * O botão se expande até ocupar a janela; então "aoCobrir" recebe um Runnable
     * "retomar", que deve ser chamado quando a ação terminar (inclusive depois de
     * operações assíncronas). A forma então se dissolve e revela a tela.
     */
    public static void executar(Node origem, Consumer<Runnable> aoCobrir) {
        Pane overlay = overlayDe(origem);
        if (!podeAnimar(overlay)) {
            aoCobrir.accept(() -> { });
            return;
        }

        ocupar(overlay);
        double[] g = geometria(overlay, origem);
        Rectangle forma = criarForma(g);
        overlay.getChildren().add(forma);

        AtomicBoolean retomado = new AtomicBoolean(false);
        Runnable retomar = () -> {
            if (retomado.compareAndSet(false, true)) {
                dissolver(overlay, forma);
            }
        };

        // Segurança: se a ação nunca chamar "retomar", libera a tela depois de 8 s
        PauseTransition guarda = new PauseTransition(Duration.seconds(8));
        guarda.setOnFinished(e -> retomar.run());

        expandir(overlay, forma, g, () -> {
            guarda.play();
            try {
                aoCobrir.accept(retomar);
            } catch (RuntimeException erro) {
                retomar.run();
                throw erro;
            }
        });
    }

    // ------------------------------------------------------------------
    // Troca de tela
    // ------------------------------------------------------------------

    /** O botão se expande até ocupar a janela e, então, "trocarTela" mostra a nova tela. */
    public static void navegar(Node origem, Runnable trocarTela) {
        Pane overlay = overlayDe(origem);
        if (!podeAnimar(overlay)) {
            trocarTela.run();
            return;
        }

        ocupar(overlay);
        double[] g = geometria(overlay, origem);
        Rectangle forma = criarForma(g);
        overlay.getChildren().add(forma);

        expandir(overlay, forma, g, () -> {
            revelarProximaTela = true;
            marcaRevelar = System.nanoTime();
            try {
                trocarTela.run();
            } catch (RuntimeException erro) {
                revelarProximaTela = false;
                liberar(overlay, forma);
                throw erro;
            }
        });
    }

    /**
     * Chamado pela nova tela logo que ela é montada. Se a tela anterior pediu uma
     * transição, cobre esta tela com a forma (já na cor de fundo, igual ao fim da
     * expansão) e a dissolve, revelando o conteúdo. Devolve true se iniciou.
     */
    public static boolean revelarSeNecessario(Pane overlay) {
        boolean valido = revelarProximaTela
                && System.nanoTime() - marcaRevelar < VALIDADE_NS;
        revelarProximaTela = false;
        if (!valido) {
            return false;
        }

        Rectangle forma = new Rectangle();
        forma.setFill(corFinal());
        forma.widthProperty().bind(overlay.widthProperty());
        forma.heightProperty().bind(overlay.heightProperty());
        overlay.getChildren().add(forma);
        ocupar(overlay);

        // Espera a janela mostrar e dimensionar a nova tela, e então dissolve
        PauseTransition espera = new PauseTransition(Duration.millis(120));
        espera.setOnFinished(e -> dissolver(overlay, forma));
        espera.play();
        return true;
    }

    // ------------------------------------------------------------------
    // Animações
    // ------------------------------------------------------------------

    /** Botão -> janela inteira: posição, tamanho, cantos e cor mudam juntos. */
    private static void expandir(Pane overlay, Rectangle forma, double[] g, Runnable aoTerminar) {
        Timeline linha = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(forma.xProperty(), g[0]),
                        new KeyValue(forma.yProperty(), g[1]),
                        new KeyValue(forma.widthProperty(), g[2]),
                        new KeyValue(forma.heightProperty(), g[3]),
                        new KeyValue(forma.arcWidthProperty(), ARCO_BOTAO),
                        new KeyValue(forma.arcHeightProperty(), ARCO_BOTAO),
                        new KeyValue(forma.fillProperty(), COR_BOTAO)),
                new KeyFrame(Duration.millis(DURACAO_EXPANDIR),
                        new KeyValue(forma.xProperty(), 0, CURVA),
                        new KeyValue(forma.yProperty(), 0, CURVA),
                        new KeyValue(forma.widthProperty(), overlay.getWidth(), CURVA),
                        new KeyValue(forma.heightProperty(), overlay.getHeight(), CURVA),
                        new KeyValue(forma.arcWidthProperty(), 0, CURVA),
                        new KeyValue(forma.arcHeightProperty(), 0, CURVA),
                        new KeyValue(forma.fillProperty(), corFinal(), CURVA))
        );

        linha.setOnFinished(e -> {
            // Daqui em diante a forma acompanha o tamanho da janela
            forma.setX(0);
            forma.setY(0);
            forma.setArcWidth(0);
            forma.setArcHeight(0);
            forma.widthProperty().bind(overlay.widthProperty());
            forma.heightProperty().bind(overlay.heightProperty());
            aoTerminar.run();
        });
        linha.play();
    }

    /** A forma some suavemente e deixa a tela nova à mostra. */
    private static void dissolver(Pane overlay, Rectangle forma) {
        FadeTransition fade = new FadeTransition(Duration.millis(DURACAO_DISSOLVER), forma);
        fade.setToValue(0);
        fade.setOnFinished(e -> liberar(overlay, forma));
        fade.play();
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static Rectangle criarForma(double[] g) {
        Rectangle forma = new Rectangle(g[0], g[1], g[2], g[3]);
        forma.setArcWidth(ARCO_BOTAO);
        forma.setArcHeight(ARCO_BOTAO);
        forma.setFill(COR_BOTAO);
        return forma;
    }

    /** A cor de fundo da tela nova: no fim da expansão a forma já "é" a tela. */
    private static Color corFinal() {
        return Color.web(Estilos.temaAtual().getCorFundo());
    }

    /** Posição e tamanho do botão, em coordenadas do overlay: {x, y, largura, altura}. */
    private static double[] geometria(Pane overlay, Node origem) {
        Bounds caixa = origem.localToScene(origem.getBoundsInLocal());
        Point2D canto = overlay.sceneToLocal(caixa.getMinX(), caixa.getMinY());
        return new double[]{canto.getX(), canto.getY(), caixa.getWidth(), caixa.getHeight()};
    }

    private static Pane overlayDe(Node no) {
        if (no == null || no.getScene() == null) {
            return null;
        }
        Object overlay = no.getScene().getRoot().getProperties().get(CHAVE_OVERLAY);
        return overlay instanceof Pane pane ? pane : null;
    }

    private static boolean podeAnimar(Pane overlay) {
        return overlay != null
                && overlay.getWidth() > 0
                && !overlay.getProperties().containsKey(OCUPADO);
    }

    /** Bloqueia cliques durante a transição. */
    private static void ocupar(Pane overlay) {
        overlay.getProperties().put(OCUPADO, Boolean.TRUE);
        overlay.setMouseTransparent(false);
    }

    private static void liberar(Pane overlay, Rectangle forma) {
        overlay.getChildren().remove(forma);
        overlay.setMouseTransparent(true);
        overlay.getProperties().remove(OCUPADO);
    }
}