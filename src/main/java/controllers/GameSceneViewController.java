package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class GameSceneViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane game;

    @FXML
    private Text player_scores;

    @FXML
    private Pane scoreboard;

    @FXML
    void initialize() {
        assert game != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert player_scores != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scoreboard != null : "fx:id=\"scoreboard\" was not injected: check your FXML file 'game-scene-view.fxml'.";

    }

}
