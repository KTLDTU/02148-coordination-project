package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("AZ Tractor game");
        stage.setResizable(false);
        GameApplication gameApplication = new GameApplication(stage);
        gameApplication.startGame(stage);
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
