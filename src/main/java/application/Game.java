package application;

import controllers.GameSceneController;
import controllers.MovementController;
import datatypes.HashSetIntArray;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

            Thread movementListener = new Thread(new MovementListener(this));
            movementListener.setDaemon(true);
            movementListener.start();
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
        gameController.initializePlayer(this);
        movementController = new MovementController(this);

        for (Integer playerID : playerIDs) {
            if (playerID != MY_PLAYER_ID) {
                tractors.put(playerID, new Rectangle(gameController.gamePane.getWidth() / (grid.COLS * 2) - PLAYER_WIDTH / 2, gameController.gamePane.getHeight() / (grid.ROWS * 2) - PLAYER_HEIGHT / 2, PLAYER_WIDTH, PLAYER_HEIGHT));
                gameController.gamePane.getChildren().add(tractors.get(playerID));
            }
        }
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
                // TODO: it's possible for a player to fetch their own position multiple times, thus not allowing other players to get the position... fix by creating a MovementListener for each player?
                // TODO: no... fix by sending not only which player position to update, but also which player should receive... should be done in MovementController... or function in Game called by MovementController
                Object[] obj = game.gameSpace.get(new ActualField("position"), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];

                // TODO?: make own player rectangle be part of the hashmap?
                if (playerID != game.MY_PLAYER_ID) {
//                    System.out.println("player " + game.MY_PLAYER_ID + " has received position from " + playerID);
                    double tractorX = (double) obj[2];
                    double tractorY = (double) obj[3];
                    double tractorRot = (double) obj[4];

                    Rectangle tractor = game.tractors.get(playerID);
                    tractor.setLayoutX(tractorX);
                    tractor.setLayoutY(tractorY);
                    tractor.setRotate(tractorRot);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
