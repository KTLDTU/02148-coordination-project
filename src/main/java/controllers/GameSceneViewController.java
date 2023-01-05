package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class GameSceneViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public Pane gamePane;

    @FXML
    private AnchorPane scene;

    @FXML
    private Text playerScores;

    @FXML
    private Pane scoreboard;

    private final MovementController movementController = new MovementController();
    Rectangle player;

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScores != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scoreboard != null : "fx:id=\"scoreboard\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        initializePlayer();
    }

    private void initializePlayer() {
        player = new Rectangle(50, 60, 70, 80);
        gamePane.getChildren().add(player);
        movementController.makeMovable(player, gamePane);
    }
}
