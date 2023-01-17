package controllers;

import application.Game;
import application.GameApplication;
import application.Grid;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.util.Map;

public class GameSceneController {

    @FXML
    public Pane gamePane;

    @FXML
    private BorderPane scene;

    @FXML
    public HBox playerScoreContainer;

    @FXML
    public Button movementPredictionOff;

    @FXML
    public Button movementPredictionOn;

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScoreContainer != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        movementPredictionOn.setFocusTraversable(false);
        movementPredictionOff.setFocusTraversable(false);
    }

    public void displayGrid(Grid grid) {
        for (var wall : grid.horizontalWalls)
            gamePane.getChildren().add(wall);

        for (var wall : grid.verticalWalls)
            gamePane.getChildren().add(wall);
    }

    public void displayPlayersNameAndScore(Map<Integer, String> playerNames, Map<Integer, Integer> playerScores) {
        Platform.runLater(() -> playerScoreContainer.getChildren().clear());
        int index = 0;

        for (Integer playerId : playerNames.keySet()) {
            String name = playerNames.get(playerId);
            HBox nameContainer = new HBox(5);
            Text nameText = new Text(name + ":");
            nameText.setFill(Game.colors.get(index++));
            Text scoreText = new Text(playerScores.get(playerId).toString());
            nameContainer.setAlignment(Pos.CENTER);
            nameContainer.setPrefWidth(GameApplication.WINDOW_WIDTH / playerNames.size());
            nameContainer.getChildren().addAll(nameText, scoreText);
            Platform.runLater(() -> playerScoreContainer.getChildren().add(nameContainer));
        }
    }
}