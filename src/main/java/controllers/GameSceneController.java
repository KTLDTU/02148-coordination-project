package controllers;

import application.Game;
import application.Grid;
import application.Player;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;

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

    // TODO: this function probably shouldn't be here
    public void initializePlayer(Game game, Player player) {
        try {
            // place player in center of upper left square
            Pair<Double, Double> startPos = new Pair<>(gamePane.getWidth() / (game.grid.COLS * 2) - Player.PLAYER_WIDTH / 2, gamePane.getHeight() / (game.grid.ROWS * 2) - Player.PLAYER_HEIGHT / 2);
            player.tractor = new Rectangle(startPos.getKey(), startPos.getValue(), Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
            gamePane.getChildren().add(player.tractor);
            game.gameSpace.put("position", player.PLAYER_ID, player.tractor.getLayoutX(), player.tractor.getLayoutY(), player.tractor.getRotate());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void displayGrid(Grid grid) {
        for (var wall : grid.walls)
            gamePane.getChildren().add(wall);
    }
}
