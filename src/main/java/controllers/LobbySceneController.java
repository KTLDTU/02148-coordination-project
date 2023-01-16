package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import application.Room;
import application.RoomCellFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class LobbySceneController {

    @FXML
    public Button createRoomButton;


    @FXML
    public ListView<Room> roomList;

    @FXML
    void initialize() {
        assert createRoomButton != null : "fx:id=\"createRoomButton\" was not injected: check your FXML file 'lobbyScene.fxml'.";
        assert roomList != null : "fx:id=\"roomList\" was not injected: check your FXML file 'lobbyScene.fxml'.";
        roomList.setCellFactory(new RoomCellFactory());
    }

}
