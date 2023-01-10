package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
    void goBackToLobby(ActionEvent event) {

    }

    @FXML
    void startGame(ActionEvent event) {

    }

    public void addPlayerToList(String name) {
        playerList.getItems().add(name);
    }


    public void updatePlayerList(ArrayList<String> newPlayerNames) {
        ObservableList<String> observableList = FXCollections.observableArrayList(newPlayerNames);
        playerList.getItems().setAll(observableList);
    }
}
