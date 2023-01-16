package application;

import controllers.JoinOrCreateController;
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

    private String HOST_IP;
    private Scene nameInputScene;
    private String name = "defaultName";

    public ApplicationIntro(Stage stage) {
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
            FXMLLoader CreateLoader = new FXMLLoader(JoinOrCreateController.class.getResource("/joinOrCreate.fxml"));
            AnchorPane scene = CreateLoader.load();
            JoinOrCreateController controller = CreateLoader.getController();
            controller.btnCreate.setOnAction(e -> {
                HOST_IP = controller.textField.getText();
                new GameApplication(stage, HOST_IP, true, name).startGame(stage);
            });
            controller.btnJoin.setOnAction(e -> {
                HOST_IP = controller.textField.getText();
                new GameApplication(stage, HOST_IP, false, name).startGame(stage);
            });
            createOrJoinScene = new Scene(scene, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT);
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
