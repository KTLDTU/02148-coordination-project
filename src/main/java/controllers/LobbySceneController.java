package controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import application.Lobby;
import application.LobbyConnector;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class LobbySceneController {

    public ListView rooms;
    public Button refresh;
    public Button createroom;
    private LobbyConnector lobby;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    @FXML
    private Label playerAmount;

    @FXML
    void initialize() throws URISyntaxException, InterruptedException, IOException {
        lobby = new LobbyConnector(null,"Jonas");
        Timeline chatUpdater = new Timeline(
                new KeyFrame(Duration.seconds(0.1),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    playerAmount.setText("Amount of players in lobby: " + lobby.getPlayerAmount());
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }));
        chatUpdater.setCycleCount(Timeline.INDEFINITE);
        chatUpdater.play();
        for(Object[] room : lobby.getRooms()){
            rooms.getItems().add(room[2]);
        }
    }

    public void onRefreshClicked(ActionEvent actionEvent) throws InterruptedException {
        rooms.getItems().remove(0,rooms.getItems().size());
        for(Object[] room : lobby.getRooms()){
            rooms.getItems().add(room[2]);
        }
    }

    public void createRoom(ActionEvent actionEvent) throws InterruptedException {
        lobby.createRoom("uri");
        // go to created room
    }

    public void selectRoom(MouseEvent mouseEvent) throws InterruptedException {
         if(mouseEvent.getClickCount() >= 2 && rooms.getSelectionModel().getSelectedItem() != null){
             playerAmount.setText("Joining room: " + lobby.joinRoom("" + rooms.getSelectionModel().getSelectedItem()));
            // go to selected room
         }
    }
}
