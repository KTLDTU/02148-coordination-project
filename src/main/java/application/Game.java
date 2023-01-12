package application;

import controllers.GameSceneController;
import controllers.MovementController;
import datatypes.HashSetIntArray;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {
    public static final double PLAYER_WIDTH = 20, PLAYER_HEIGHT = 15;
    public GameSceneController gameController;
    public Scene gameScene;
    public Grid grid;
    public Space gameSpace;
    public HashMap<Integer, Rectangle> tractors;
    public Rectangle myTractor;
    public ArrayList<Integer> playerIDs;
    public final int MY_PLAYER_ID;
    private MovementController movementController;

    public Game(Stage stage, Space gameSpace, ArrayList<Integer> playerIDs, int MY_PLAYER_ID) {
        try {
            this.gameSpace = gameSpace;
            this.playerIDs = playerIDs;
            this.MY_PLAYER_ID = MY_PLAYER_ID;

            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gameScene = new Scene(scene);
            stage.setScene(gameScene);
            tractors = new HashMap<>();

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

    public void spawnPlayers() {
        for (Integer playerID : playerIDs) {
            Rectangle newTractor = (playerID == MY_PLAYER_ID ? randomSpawn() : new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT));
            tractors.put(playerID, newTractor);
            gameController.gamePane.getChildren().add(tractors.get(playerID));
        }

        myTractor = tractors.get(MY_PLAYER_ID);
        movementController = new MovementController(this);

        Thread movementListener = new Thread(new MovementListener(this));
        movementListener.setDaemon(true);
        movementListener.start();

    }

    private Rectangle randomSpawn() {
        Random random = new Random();
        double offsetX = gameController.gamePane.getWidth() / (grid.COLS * 2) - PLAYER_WIDTH / 2;
        double offsetY = gameController.gamePane.getHeight() / (grid.ROWS * 2) - PLAYER_HEIGHT / 2;
        double col = random.nextInt(grid.COLS);
        double row = random.nextInt(grid.ROWS);
        double x = gameController.gamePane.getWidth() * col / grid.COLS + offsetX;
        double y = gameController.gamePane.getHeight() * row / grid.ROWS + offsetY;
        int rotation = random.nextInt(360);

        Rectangle tractor = new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT);
        tractor.setLayoutX(x);
        tractor.setLayoutY(y);
        tractor.setRotate(rotation);
        tractor.setFill(Color.ROYALBLUE); // color to distinguish from other tractors (temporary - all should have different colors)
        return tractor;
    }

}

class MovementListener implements Runnable {
    private Game game;

    public MovementListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("position"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];
                double tractorX = (double) obj[3];
                double tractorY = (double) obj[4];
                double tractorRot = (double) obj[5];
//                System.out.println("Player " + game.MY_PLAYER_ID + " has received position (" + tractorX + ", " + tractorY + ") from " + playerID);

                Rectangle tractor = game.tractors.get(playerID);

                Platform.runLater(() -> {
                    tractor.setLayoutX(tractorX);
                    tractor.setLayoutY(tractorY);
                    tractor.setRotate(tractorRot);
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
