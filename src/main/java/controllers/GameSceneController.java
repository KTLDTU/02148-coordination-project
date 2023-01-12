package controllers;

import application.Grid;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class GameSceneController {

    @FXML
    public Pane gamePane;

    @FXML
    private BorderPane scene;

    @FXML
    private Text playerScores;

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScores != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'game-scene-view.fxml'.";
    }

    public void displayGrid(Grid grid) {
        for (var wall : grid.horizontalWalls)
            gamePane.getChildren().add(wall);

        for (var wall : grid.verticalWalls)
            gamePane.getChildren().add(wall);
    }
}
