package application;

import javafx.application.Application;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws URISyntaxException, InterruptedException {
        stage.setTitle("AZ Tractor game");
        stage.setResizable(false);
        ApplicationIntro applicationIntro = new ApplicationIntro(stage);
        applicationIntro.startIntro(stage);
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
