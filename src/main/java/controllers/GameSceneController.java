package controllers;

import java.util.ArrayList;

import application.Grid;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class GameSceneController {

    @FXML
    private Pane gamePane;

    @FXML
    private BorderPane scene;

    @FXML
    private Text playerScores;

    private Grid grid;

    Rectangle player;
    ArrayList<Rectangle> walls = new ArrayList<>();

    @FXML
    void initialize() {
        assert gamePane != null : "fx:id=\"game\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert playerScores != null : "fx:id=\"player_scores\" was not injected: check your FXML file 'game-scene-view.fxml'.";
        assert scene != null : "fx:id=\"scene\" was not injected: check your FXML file 'game-scene-view.fxml'.";
    }

    public Rectangle initializePlayer() {
//        player = new Player();
        // place player in center of upper left square
        int playerWidth = 20, playerHeight = 15;
        Pair<Double, Double> startPos = new Pair<>(gamePane.getWidth() / (grid.COLS * 2) - playerWidth / 2, gamePane.getHeight() / (grid.ROWS * 2) - playerHeight / 2);
        player = new Rectangle(startPos.getKey(), startPos.getValue(), playerWidth, playerHeight);
        gamePane.getChildren().add(player);
        return player;
    }

    public void displayGrid(Grid grid) {
        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();
        double wallThickness = 2.0;
        double wallWidth = paneWidth / grid.COLS + wallThickness;
        double wallHeight = paneHeight / grid.ROWS + wallThickness;

        // outer walls
        walls.add(new Rectangle(0, 0, paneWidth, wallThickness));
        walls.add(new Rectangle(0, paneHeight, paneWidth + wallThickness, wallThickness));
        walls.add(new Rectangle(0, 0, wallThickness, paneHeight));
        walls.add(new Rectangle(paneWidth, 0, wallThickness, paneHeight + wallThickness));

        // inner walls
        for (int row = 0; row < grid.ROWS; row++) {
            for (int col = 0; col < grid.COLS; col++) {
                Pair<Integer, Integer> p1, p2;

                // add wall below
                if (row < grid.ROWS - 1) {
                    p1 = new Pair<>(row, col);
                    p2 = new Pair<>(row + 1, col);

                    if (grid.notConnected(p1, p2)) {
                        double x = paneWidth * col / grid.COLS;
                        double y = paneHeight * (row + 1) / grid.ROWS;
                        addInnerWall(new Rectangle(x, y, wallWidth, wallThickness));
                    }
                }

                // add wall to the right
                if (col < grid.COLS - 1) {
                    p1 = new Pair<>(row, col);
                    p2 = new Pair<>(row, col + 1);

                    if (grid.notConnected(p1, p2)) {
                        double x = paneWidth * (col + 1) / grid.COLS;
                        double y = paneHeight * row / grid.ROWS;
                        addInnerWall(new Rectangle(x, y, wallThickness, wallHeight));
                    }
                }
            }
        }

        for (var wall : walls)
            gamePane.getChildren().add(wall);
    }


    private void addInnerWall(Rectangle wall) {
        wall.setFill(Color.DIMGRAY);
        walls.add(wall);
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }
}
