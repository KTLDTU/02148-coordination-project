package application;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class Game {

    private Scene lobbyScene;
    private Scene gameScene;
    private Scene startScene;
    private static int windowWidth = 960;
    private static int windowHeight = 540;

    public Game(Stage stage) throws IOException {
        makeStartScreen(stage);
        makeLobbyScreen(stage);
        makeGameScreen(stage);
    }

    public void startGame(Stage stage) {
        showStartScreen(stage);
        stage.show();
    }

    private void showStartScreen(Stage stage) {
        stage.setScene(startScene);
        stage.centerOnScreen();
    }

    private void makeStartScreen(Stage stage) {
        Label gameTitle = new Label("AZ");

        // Make buttons
        Button lobbyButton = new Button("Start lobby");
        lobbyButton.setPrefSize(150, 30);
        lobbyButton.setOnAction(e -> stage.setScene(lobbyScene));
        Button gameButton = new Button("Start game");
        gameButton.setPrefSize(150, 30);
        gameButton.setOnAction(e -> stage.setScene(gameScene));
        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, gameButton, exitButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, windowWidth, windowHeight);
    }

    private void launchGame(Stage stage) {
    }

    private void makeLobbyScreen(Stage stage) {
        Text chat = new Text();
        chat.setText("Chat");

        BorderPane lobbyLayout = new BorderPane();
        lobbyLayout.setRight(chat);

        lobbyScene = new Scene(lobbyLayout, windowWidth, windowWidth);
    }

    private void makeGameScreen(Stage stage) throws IOException {
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
        Parent game = gameLoader.load();
        gameScene = new Scene(game);
    }
}
