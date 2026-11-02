package br.com.chatia.util;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;

/** Animações reutilizáveis da interface (JavaFX). */
public final class Animacoes {

    private static final String CHAVE_EFEITO = "efeito-clique-instalado";
    private static final String CHAVE_PRESSIONADO = "botao-pressionado";
    private static final String CHAVE_ANIM_CLIQUE = "anim-clique";
    private static final String CHAVE_ANIM = "anim";

    private Animacoes() {
    }

    // ---------------------------------------------------------------- fade

    public static void fadeIn(Node no, double milissegundos) {
        no.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(milissegundos), no);
        fade.setToValue(1);
        fade.play();
    }

    public static void fadeOut(Node no, double milissegundos, Runnable aoTerminar) {
        FadeTransition fade = new FadeTransition(Duration.millis(milissegundos), no);
        fade.setToValue(0);
        fade.setOnFinished(e -> aoTerminar.run());
        fade.play();
    }
    /** O conteúdo sobe suavemente e aparece (usado quando uma tela nova é revelada). */
    public static void aparecer(Node no, double atrasoMilissegundos) {
        no.setOpacity(0);
        no.setTranslateY(26);

        FadeTransition fade = new FadeTransition(Duration.millis(420), no);
        fade.setToValue(1);

        TranslateTransition mover = new TranslateTransition(Duration.millis(480), no);
        mover.setToY(0);
        mover.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition conjunto = new ParallelTransition(fade, mover);
        conjunto.setDelay(Duration.millis(atrasoMilissegundos));
        conjunto.play();
    }

    // ------------------------------------------------------------- balões

    /** A bolha entra deslizando e aparecendo (do lado de quem enviou). */
    public static void entrarBolha(Node bolha, boolean doUsuario) {
        bolha.setOpacity(0);
        bolha.setTranslateX(doUsuario ? 28 : -28);
        bolha.setTranslateY(10);

        FadeTransition fade = new FadeTransition(Duration.millis(260), bolha);
        fade.setToValue(1);

        TranslateTransition mover = new TranslateTransition(Duration.millis(300), bolha);
        mover.setToX(0);
        mover.setToY(0);
        mover.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, mover).play();
    }

    /** Pontinhos pulando em sequência (indicador de "digitando"). */
    public static Timeline animarPontos(List<Circle> pontos) {
        Timeline linha = new Timeline();
        linha.setCycleCount(Animation.INDEFINITE);

        for (int i = 0; i < pontos.size(); i++) {
            Circle ponto = pontos.get(i);
            double inicio = i * 150;

            linha.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(inicio),
                            new KeyValue(ponto.translateYProperty(), 0),
                            new KeyValue(ponto.opacityProperty(), 0.35)),
                    new KeyFrame(Duration.millis(inicio + 250),
                            new KeyValue(ponto.translateYProperty(), -6, Interpolator.EASE_OUT),
                            new KeyValue(ponto.opacityProperty(), 1)),
                    new KeyFrame(Duration.millis(inicio + 500),
                            new KeyValue(ponto.translateYProperty(), 0, Interpolator.EASE_IN),
                            new KeyValue(ponto.opacityProperty(), 0.35))
            );
        }
        // Pausa no fim do ciclo
        linha.getKeyFrames().add(new KeyFrame(Duration.millis(1100)));
        linha.play();
        return linha;
    }

    /** Para a animação guardada em um nó (evita animação rodando sem necessidade). */
    public static void parar(Node no) {
        Object anim = no.getProperties().get(CHAVE_ANIM);
        if (anim instanceof Animation animacao) {
            animacao.stop();
        }
    }

    public static void guardar(Node no, Animation animacao) {
        no.getProperties().put(CHAVE_ANIM, animacao);
    }

    /** Efeito de "digitando": mostra o texto aos poucos (dura no máximo ~2,5 s). */
    public static void digitar(Label label, String texto) {
        int total = texto.length();
        if (total == 0) {
            return;
        }
        int porQuadro = Math.max(1, (int) Math.ceil(total / 140.0));
        int[] mostrados = {0};

        Timeline linha = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            mostrados[0] = Math.min(total, mostrados[0] + porQuadro);
            label.setText(texto.substring(0, mostrados[0]));
        }));
        linha.setCycleCount((int) Math.ceil(total / (double) porQuadro));
        linha.setOnFinished(e -> label.setText(texto));
        linha.play();
    }

    // ------------------------------------------------------ clique nos botões

    /**
     * Instala, UMA vez por cena, o efeito de clique em todos os botões:
     * ao apertar o botão encolhe um pouco; ao soltar, volta com um leve "pulo".
     */
    public static void instalarEfeitoClique(Scene scene) {
        if (scene.getProperties().putIfAbsent(CHAVE_EFEITO, Boolean.TRUE) != null) {
            return;
        }

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Button botao = botaoDe(e.getTarget());
            if (botao != null && !botao.isDisabled()
                    && !botao.getStyleClass().contains("link")) {
                scene.getProperties().put(CHAVE_PRESSIONADO, botao);
                escalar(botao, 0.95, 90);
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            Object pressionado = scene.getProperties().remove(CHAVE_PRESSIONADO);
            if (pressionado instanceof Button botao) {
                soltar(botao);
            }
        });
    }

    private static Button botaoDe(Object alvo) {
        Node no = alvo instanceof Node ? (Node) alvo : null;
        while (no != null && !(no instanceof Button)) {
            no = no.getParent();
        }
        return (Button) no;
    }

    private static void escalar(Node no, double escala, double milissegundos) {
        ScaleTransition transicao = new ScaleTransition(Duration.millis(milissegundos), no);
        transicao.setToX(escala);
        transicao.setToY(escala);
        transicao.setInterpolator(Interpolator.EASE_OUT);
        substituirAnimacaoDeClique(no, transicao);
        transicao.play();
    }

    private static void soltar(Node no) {
        ScaleTransition sobe = new ScaleTransition(Duration.millis(90), no);
        sobe.setToX(1.04);
        sobe.setToY(1.04);

        ScaleTransition volta = new ScaleTransition(Duration.millis(110), no);
        volta.setToX(1);
        volta.setToY(1);

        SequentialTransition sequencia = new SequentialTransition(sobe, volta);
        substituirAnimacaoDeClique(no, sequencia);
        sequencia.play();
    }

    private static void substituirAnimacaoDeClique(Node no, Animation nova) {
        Object anterior = no.getProperties().put(CHAVE_ANIM_CLIQUE, nova);
        if (anterior instanceof Animation animacao) {
            animacao.stop();
        }

    }
}