package application;

import controllers.GameSceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Game {
    public GameSceneController gameController;
    public Scene gameScene;
    public Grid grid;
    private ArrayList<Player> players;

    public Game(Stage stage) {
        try {
            players = new ArrayList<>();
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gameScene = new Scene(scene);
            stage.setScene(gameScene);

            grid = new Grid(gameController.gamePane);
            gameController.displayGrid(grid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
}
