package controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.Grid;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class GameSceneViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane gamePane;

    @FXML
    private BorderPane scene;

    @FXML
    private Text playerScores;

    private final MovementController movementController = new MovementController();
    Rectangle player;
    ArrayList<Rectangle> walls = new ArrayList<>();

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScores != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        initializePlayer();
    }

    private void initializePlayer() {
        player = new Rectangle(50, 60, 70, 80);
        gamePane.getChildren().add(player);
        movementController.makeMovable(player, scene);
    }

    public void displayGrid(Grid grid) {
        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();
        double wallThickness = 2.0, wallLength = paneWidth / grid.COLS + wallThickness;

        // outer walls
        walls.add(new Rectangle(0, 0, paneWidth, wallThickness));
        walls.add(new Rectangle(0, paneHeight, paneWidth + wallThickness, wallThickness));
        walls.add(new Rectangle(0, 0, wallThickness, paneHeight));
        walls.add(new Rectangle(paneWidth, 0, wallThickness, paneHeight + wallThickness));

        // inner walls
        for (int row = 0; row < grid.ROWS; row++) {
            for (int col = 0; col < grid.COLS; col++) {
                Pair<Integer, Integer> p1, p2;

                if (row < grid.ROWS - 1) {
                    p1 = new Pair<>(row, col);
                    p2 = new Pair<>(row+1, col);

                    if (notConnected(grid, p1, p2)) {
                        double x = paneWidth * col / grid.COLS;
                        double y = paneHeight * (row + 1) / grid.ROWS;
                        addInnerWall(new Rectangle(x, y, wallLength, wallThickness));
                    }
                }

                if (col < grid.COLS - 1) {
                    p1 = new Pair<>(row, col);
                    p2 = new Pair<>(row, col+1);

                    if (notConnected(grid, p1, p2)) {
                        double x = paneWidth * (col + 1) / grid.COLS;
                        double y = paneHeight * row / grid.ROWS;
                        addInnerWall(new Rectangle(x, y, wallThickness, wallLength));
                    }
                }
            }
        }

        for (var wall : walls)
            gamePane.getChildren().add(wall);
    }

    private boolean notConnected(Grid grid, Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
        return !grid.connected.contains(new Pair<>(p1, p2)) && !grid.connected.contains(new Pair<>(p2, p1));
    }


    private void addInnerWall(Rectangle wall) {
        wall.setFill(Color.DIMGRAY);
        walls.add(wall);
    }
}
