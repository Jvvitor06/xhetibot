package br.com.chatia.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Executa um trabalho pesado fora da thread da tela e devolve o resultado nela. */
public final class Tarefa {

    private Tarefa() {
    }

    public static <T> void executar(Callable<T> trabalho,
                                    Consumer<T> aoSucesso,
                                    Consumer<String> aoErro) {

        Task<T> tarefa = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabalho.call();
            }
        };

        tarefa.setOnSucceeded(e -> aoSucesso.accept(tarefa.getValue()));
        tarefa.setOnFailed(e -> aoErro.accept(MensagemErro.de(tarefa.getException())));

        Thread thread = new Thread(tarefa, "tarefa-background");
        thread.setDaemon(true);
        thread.start();
    }
}