package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.ArrayList;

public class RoomSceneViewController {

    @FXML
    private Button backToLobbyButton;

    @FXML
    private ListView<String> playerList;

    @FXML
    private Label roomNameText;

    @FXML
    private Button startGameButton;

    @FXML
    void initialize() {
        assert backToLobbyButton != null : "fx:id=\"backToLobbyButton\" was not injected: check your FXML file 'room-scene-view.fxml'.";
        assert startGameButton != null : "fx:id=\"startGameButton\" was not injected: check your FXML file 'room-scene-view.fxml'.";
        assert playerList != null : "fx:id=\"playerList\" was not injected: check your FXML file 'room-scene-view.fxml'.";
        assert roomNameText != null : "fx:id=\"roomNameText\" was not injected: check your FXML file 'room-scene-view.fxml'.";

        playerList.setMouseTransparent(true);
        playerList.setFocusTraversable(false);
    }

    public void updatePlayerList(ArrayList<String> newPlayerNames) {
        ObservableList<String> observableList = FXCollections.observableArrayList(newPlayerNames);
        Platform.runLater(() -> playerList.getItems().setAll(observableList));
    }

    public void setRoomNameText(String name) {
        roomNameText.setText(name + "'s room");
    }
}
