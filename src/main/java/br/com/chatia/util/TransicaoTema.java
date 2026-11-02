package br.com.chatia.util;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Transform;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Troca de tema com um "amanhecer": uma luz se expande em círculo a partir do botão
 * e, por onde ela passa, a tela já aparece no tema novo.
 */
public final class TransicaoTema {

    private static final String OCUPADO = "transicao-tema-ocupada";

    private static final double DURACAO = 1100;        // milissegundos
    private static final double LARGURA_FORA = 150;    // brilho à frente da borda
    private static final double LARGURA_DENTRO = 70;   // brilho atrás da borda
    private static final double INTENSIDADE = 0.65;    // força do brilho (0 a 1)

    private static final Color LUZ_DIA = Color.web("#ffb86b");    // amanhecer (tema claro)
    private static final Color LUZ_NOITE = Color.web("#1f9cf0");  // azul da marca (tema escuro)

    private TransicaoTema() {
    }

    /**
     * @param origem     o botão clicado (de onde a luz nasce)
     * @param trocarTema ação que efetivamente troca o tema (é executada por baixo da animação)
     */
    public static void executar(Node origem, Runnable trocarTema) {
        Scene scene = origem.getScene();
        Pane raiz = (scene != null && scene.getRoot() instanceof Pane pane) ? pane : null;

        // Sem tela, sem tamanho ou já em animação: troca direto, sem efeito
        if (raiz == null || raiz.getWidth() <= 0 || raiz.getProperties().containsKey(OCUPADO)) {
            trocarTema.run();
            return;
        }
        raiz.getProperties().put(OCUPADO, Boolean.TRUE);

        boolean trocou = false;
        try {
            // Ponto de onde a luz nasce (centro do botão)
            Bounds caixa = origem.localToScene(origem.getBoundsInLocal());
            Point2D centro = raiz.sceneToLocal(caixa.getCenterX(), caixa.getCenterY());
            double cx = centro.getX();
            double cy = centro.getY();
            double raioMaximo = Math.hypot(
                    Math.max(cx, raiz.getWidth() - cx),
                    Math.max(cy, raiz.getHeight() - cy)) + LARGURA_FORA + LARGURA_DENTRO;

            // 1) foto da tela com o tema antigo
            ImageView antes = foto(raiz, Estilos.temaAtual());

            // 2) troca o tema por baixo e tira outra foto (tema novo)
            trocarTema.run();
            trocou = true;
            Tema temaNovo = Estilos.temaAtual();
            ImageView depois = foto(raiz, temaNovo);
            Color luz = temaNovo == Tema.CLARO ? LUZ_DIA : LUZ_NOITE;

            // 3) a foto nova só aparece dentro de um círculo que cresce
            Circle revelacao = new Circle(cx, cy, 0);
            depois.setClip(revelacao);

            Circle halo = new Circle(cx, cy, LARGURA_FORA);
            halo.setMouseTransparent(true);

            Pane camada = new Pane(antes, depois, halo);
            raiz.getChildren().add(camada); // também bloqueia cliques durante a animação

            Transition animacao = new Transition() {
                {
                    setCycleDuration(Duration.millis(DURACAO));
                    setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));
                }

                @Override
                protected void interpolate(double t) {
                    double r = t * raioMaximo;
                    revelacao.setRadius(r);

                    double raioHalo = r + LARGURA_FORA;
                    halo.setRadius(raioHalo);
                    // O brilho vai se apagando perto do fim
                    halo.setFill(gradiente(cx, cy, r, raioHalo, luz, INTENSIDADE * (1 - t * t)));
                }
            };
            animacao.setOnFinished(e -> {
                raiz.getChildren().remove(camada);
                raiz.getProperties().remove(OCUPADO);
            });
            animacao.play();

        } catch (RuntimeException erro) {
            // Se algo falhar, a troca de tema continua valendo, só sem a animação
            raiz.getProperties().remove(OCUPADO);
            if (!trocou) {
                trocarTema.run();
            }
            System.err.println("[Tema] Animação indisponível: " + erro);
        }
    }

    /** "Foto" da tela inteira, já na resolução real do monitor. */
    private static ImageView foto(Pane raiz, Tema tema) {
        Window janela = raiz.getScene().getWindow();
        double escalaX = janela != null ? janela.getOutputScaleX() : 1;
        double escalaY = janela != null ? janela.getOutputScaleY() : 1;

        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setTransform(Transform.scale(escalaX, escalaY));
        parametros.setFill(Color.web(tema.getCorFundo()));

        WritableImage imagem = raiz.snapshot(parametros, null);

        ImageView vista = new ImageView(imagem);
        vista.setFitWidth(raiz.getWidth());
        vista.setFitHeight(raiz.getHeight());
        vista.setSmooth(true);
        return vista;
    }

    /** Halo: transparente por dentro, mais forte na borda do círculo e transparente por fora. */
    private static RadialGradient gradiente(double cx, double cy, double r, double raioHalo,
                                            Color luz, double intensidade) {
        double inicio = Math.max(0, (r - LARGURA_DENTRO) / raioHalo);
        double pico = r / raioHalo;
        Color transparente = luz.deriveColor(0, 1, 1, 0);
        Color forte = luz.deriveColor(0, 1, 1, Math.max(0, Math.min(1, intensidade)));

        return new RadialGradient(0, 0, cx, cy, raioHalo, false, CycleMethod.NO_CYCLE,
                new Stop(0, transparente),
                new Stop(inicio, transparente),
                new Stop(pico, forte),
                new Stop(1, transparente));
    }
}