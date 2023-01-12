package application;

import controllers.JoinOrCreate;
import controllers.LobbySceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ApplicationIntro {
    private Scene createOrJoinScene;
    public static Scene lobbyScene;
    private boolean createLobby;
    public String HOST_IP;

    public ApplicationIntro(Stage stage) {
        makeLobbyScene(stage);
        makeCreateJoinScene(stage);
    }

    public void startIntro(Stage stage) {
        showIntroScene(stage);
        stage.show();
    }

    private void showIntroScene(Stage stage) {
        stage.setScene(createOrJoinScene);
        stage.centerOnScreen();
    }

    private void makeCreateJoinScene(Stage stage){
        try {
            FXMLLoader CreateLoader = new FXMLLoader(JoinOrCreate.class.getResource("/joinOrCreate.fxml"));
            AnchorPane scene = CreateLoader.load();
            JoinOrCreate a = CreateLoader.getController();
            a.btnCreate.setOnAction(e -> {
                stage.setScene(lobbyScene);
                HOST_IP = a.textField.getText();
                createLobby = true;
                new GameApplication(stage, HOST_IP, createLobby).startGame(stage);
            });
            a.btnJoin.setOnAction(e -> {
                stage.setScene(lobbyScene);
                HOST_IP = a.textField.getText();
                createLobby = false;
                new GameApplication(stage, HOST_IP, createLobby).startGame(stage);
            });
            createOrJoinScene = new Scene(scene, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
