package application;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import controller.ChatBoxViewController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Game {

    private Scene lobbyScene;
    private Scene gameScene;
    private Scene startScene;
    private static int windowWidth = 960;
    private static int windowHeight = 540;

    public Game(Stage stage) {
        makeStartScene(stage);
        makeRoomScene(stage);
        makeGameScene(stage);
    }

    public void startGame(Stage stage) {
        showStartScene(stage);
        stage.show();
    }

    private void showStartScene(Stage stage) {
        stage.setScene(startScene);
        stage.centerOnScreen();
    }

    private void makeStartScene(Stage stage) {
        Label gameTitle = new Label("AZ");

        // Make buttons
        Button lobbyButton = new Button("Start lobby");
        lobbyButton.setPrefSize(150, 30);
        lobbyButton.setOnAction(e -> stage.setScene(lobbyScene));
        Button optionsButton = new Button("Start game");
        optionsButton.setPrefSize(150, 30);
        optionsButton.setOnAction(e -> stage.setScene(gameScene));
        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, optionsButton, exitButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, windowWidth, windowHeight);
    }

    private void launchGame(Stage stage) {
    }

    private void makeRoomScene(Stage stage) {
        BorderPane lobbyLayout = new BorderPane();

        // Chatbox resource file name
        String fileName = "/ChatboxView.fxml";
        try {
            FXMLLoader chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(fileName));
            BorderPane chatbox = chatboxLoader.load();
            lobbyLayout.setRight(chatbox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lobbyScene = new Scene(lobbyLayout, windowWidth, windowHeight);
    }

    private void makeGameScene(Stage stage) {
    }
}
