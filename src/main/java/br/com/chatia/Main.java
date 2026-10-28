package br.com.chatia;

import br.com.chatia.view.splashView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        new splashView().show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}