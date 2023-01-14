package application;

import controllers.JoinOrCreate;
import controllers.LobbySceneController;
import controllers.PlayerNameInputController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ApplicationIntro {
    // Made static for simplicity. May not be optimal
    public static Scene createOrJoinScene;
    public static Scene lobbyScene;

    public String HOST_IP;
    private Scene nameInputScene;
    private String name;

    public ApplicationIntro(Stage stage) {
        makeLobbyScene(stage);
        makeCreateJoinScene(stage);
        makeNameInputScene(stage);
    }

    public void startIntro(Stage stage) {
        showIntroScene(stage);
        stage.show();
    }

    private void showIntroScene(Stage stage) {
        stage.setScene(nameInputScene);
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
                new GameApplication(stage, HOST_IP, true, name).startGame(stage);
            });
            a.btnJoin.setOnAction(e -> {
                stage.setScene(lobbyScene);
                HOST_IP = a.textField.getText();
                new GameApplication(stage, HOST_IP, false, name).startGame(stage);
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

    private void makeNameInputScene(Stage stage) {
        try {
            FXMLLoader playerInputLoader = new FXMLLoader(PlayerNameInputController.class.getResource("/player-name-input.fxml"));
            VBox scene = playerInputLoader.load();
            PlayerNameInputController playerNameInputController = playerInputLoader.getController();
            playerNameInputController.continueButton.setOnAction(e -> {
                String nameInput = playerNameInputController.inputNameField.getText().trim();
                if (!nameInput.isEmpty()) {
                    name = nameInput;
                }
                stage.setScene(createOrJoinScene);
            });
            nameInputScene = new Scene(scene, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
