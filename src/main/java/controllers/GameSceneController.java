package controllers;

import application.Game;
import application.GameApplication;
import application.Grid;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.Collection;
import java.util.Map;

public class GameSceneController {

    @FXML
    public Pane gamePane;

    @FXML
    private BorderPane scene;

    @FXML
    private HBox playerScoreContainer;

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScoreContainer != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'game-scene-view.fxml'.";
    }

    public void displayGrid(Grid grid) {
        for (var wall : grid.horizontalWalls)
            gamePane.getChildren().add(wall);

        for (var wall : grid.verticalWalls)
            gamePane.getChildren().add(wall);
    }

    public void initializePlayerNames(Map<Integer, String> playerIdNameMap) {
        for (Map.Entry<Integer, String> entry : playerIdNameMap.entrySet()) {
            HBox nameContainer = new HBox(5);
            Text nameText = new Text(entry.getValue() + ":");
            nameText.setFill(Game.colors.get(entry.getKey()));
            Text scoreText = new Text("0");

            nameContainer.setAlignment(Pos.CENTER);
            nameContainer.setPrefWidth(GameApplication.WINDOW_WIDTH / playerIdNameMap.size());
            nameContainer.getChildren().addAll(nameText, scoreText);
            Platform.runLater(() -> playerScoreContainer.getChildren().add(nameContainer));
        }
    }
}
