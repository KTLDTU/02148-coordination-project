package application;

import controllers.GameSceneController;
import controllers.LobbySceneController;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import controllers.ChatBoxViewController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Game {

    private Scene roomScene;
    private Scene gameScene;
    private Scene startScene;
    private Scene lobbyScene;
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 540;
    private GameSceneController gameController;
    private Grid grid;

    public Game(Stage stage) {
        makeStartScene(stage);
        makeRoomScene(stage);
        makeGameScene(stage);
        makeLobbyScene(stage);
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
        Button chatButton = new Button("Start chat");
        chatButton.setPrefSize(150, 30);
        chatButton.setOnAction(e -> stage.setScene(roomScene));
        Button gameButton = new Button("Start game");
        gameButton.setPrefSize(150, 30);
        gameButton.setOnAction(e -> launchGame(stage));
        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, chatButton, gameButton, exitButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void launchGame(Stage stage) {
        stage.setScene(gameScene);
        grid = new Grid(gameScene);
        gameController.displayGrid(grid);
        gameController.initializePlayer(grid);
        gameScene.getRoot().requestFocus();
    }

    private void makeGameScene(Stage stage) {
        try {
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gameScene = new Scene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeRoomScene(Stage stage) {
        BorderPane roomLayout = new BorderPane();

        // Chatbox resource file name
        String fileName = "/ChatboxView.fxml";
        try {
            FXMLLoader chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(fileName));
            BorderPane chatbox = chatboxLoader.load();
            chatbox.setMargin(chatbox, new Insets(0,2, 0,0));
            roomLayout.setRight(chatbox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        roomScene = new Scene(roomLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }


    private void makeLobbyScene(Stage stage) {
        try {
            FXMLLoader lobbyLoader = new FXMLLoader(LobbySceneController.class.getResource("/lobbyScene.fxml"));
            AnchorPane scene = lobbyLoader.load();
            LobbySceneController lobbyController = lobbyLoader.getController();
            lobbyScene = new Scene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
