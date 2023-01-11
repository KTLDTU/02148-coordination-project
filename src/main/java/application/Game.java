package application;

import controllers.GameSceneController;
import datatypes.HashSetIntArray;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Game {
    public GameSceneController gameController;
    public Scene gameScene;
    public Grid grid;

    public Game(Stage stage) {
        try {
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gameScene = new Scene(scene);
            stage.setScene(gameScene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeGrid() {
        grid = new Grid(gameController.gamePane);
        gameController.displayGrid(grid);
    }

    public void setGrid(HashSetIntArray connectedSquares) {
        grid = new Grid(gameController.gamePane, connectedSquares);
        gameController.displayGrid(grid);
    }
}
