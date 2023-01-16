package application;

import broadcasters.PlayerPositionBroadcaster;
import controllers.GameSceneController;
import controllers.MovementController;
import controllers.ShotController;
import datatypes.HashSetIntArray;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import listeners.*;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Game {
    public static final double PLAYER_WIDTH = 20, PLAYER_HEIGHT = 15;
    public GameSceneController gameController;
    public Scene gameScene;
    public Pane gamePane;
    public Grid grid;
    public Space gameSpace;
    public HashMap<Integer, Rectangle> tractors;
    public Rectangle myTractor;
    public Map<Integer, String> playersIdNameMap;
    public final int MY_PLAYER_ID;
    public ShotController shotController;
    public HashMap<Integer, Shot> shots;
    public InputListener inputListener;

    public Game(Stage stage, Space gameSpace, Map<Integer, String> playersIdNameMap, int MY_PLAYER_ID) {
        try {
            this.gameSpace = gameSpace;
            this.MY_PLAYER_ID = MY_PLAYER_ID;
            this.playersIdNameMap = playersIdNameMap;

            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gamePane = gameController.gamePane;
            gameScene = new Scene(scene);
            stage.setScene(gameScene);

            if (GameApplication.isHost) {
                try {
                    gameSpace.put("shot id", 0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            gameController.initializePlayerNames(playersIdNameMap.values());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setGrid(HashSetIntArray connectedSquares) {
        grid = new Grid(gamePane, connectedSquares);
        Platform.runLater(() -> gameController.displayGrid(grid));
    }

    public void spawnPlayers() {
        for (Integer playerID : playersIdNameMap.keySet()) {
            Rectangle newTractor = (playerID == MY_PLAYER_ID ? randomSpawn() : new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT));
            tractors.put(playerID, newTractor);
            Platform.runLater(() -> gamePane.getChildren().add(tractors.get(playerID))); // TODO: this line gives NullPointerException occasionally
        }

        myTractor = tractors.get(MY_PLAYER_ID);
        MovementController movementController = new MovementController(this);
        shotController = new ShotController(this);
        inputListener = new InputListener(this, movementController, shotController);

        Thread movementListener = new Thread(new MovementListener(this));
        movementListener.setDaemon(true);
        movementListener.start();

        Thread shotListener = new Thread(new ShotListener(this));
        shotListener.setDaemon(true);
        shotListener.start();

        Thread killListener = new Thread(new KillListener(this));
        killListener.setDaemon(true);
        killListener.start();

        Thread gameEndListener = new Thread(new GameEndListener(this));
        gameEndListener.setDaemon(true);
        gameEndListener.start();
    }

    private Rectangle randomSpawn() {
        Random random = new Random();
        double offsetX = gamePane.getWidth() / (grid.COLS * 2) - PLAYER_WIDTH / 2;
        double offsetY = gamePane.getHeight() / (grid.ROWS * 2) - PLAYER_HEIGHT / 2;
        double col = random.nextInt(grid.COLS);
        double row = random.nextInt(grid.ROWS);
        double x = gamePane.getWidth() * col / grid.COLS + offsetX;
        double y = gamePane.getHeight() * row / grid.ROWS + offsetY;
        int rotation = random.nextInt(360);

        Rectangle tractor = new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT);
        tractor.setLayoutX(x);
        tractor.setLayoutY(y);
        tractor.setRotate(rotation);
        tractor.setFill(Color.ROYALBLUE); // color to distinguish from other tractors (temporary - all should have different colors)
        return tractor;
    }

    public int numPlayersAlive() {
        return tractors.size();
    }

    public void incrementPlayerScores(Integer playerID) {
        // TODO
    }

    public void newRound() {
        Platform.runLater(() -> gamePane.getChildren().clear());
        tractors = new HashMap<>();
        shots = new HashMap<>();

        try {
            if (GameApplication.isHost) {
                Grid grid = new Grid(gamePane);

                for (int playerID : playersIdNameMap.keySet())
                    gameSpace.put("connected squares", playerID, grid.connectedSquares);
            }

            HashSetIntArray connectedSquares = (HashSetIntArray) gameSpace.get(new ActualField("connected squares"), new ActualField(MY_PLAYER_ID), new FormalField(HashSetIntArray.class))[2];
            setGrid(connectedSquares);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        spawnPlayers();
        new Thread(new PlayerPositionBroadcaster(this)).start();
    }
}

class GameEndTimer implements Runnable {
    private final int GAME_END_DELAY_IN_MS = 2000;
    private Game game;

    public GameEndTimer(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(GAME_END_DELAY_IN_MS);

            for (int playerID : game.playersIdNameMap.keySet())
                game.gameSpace.put("game end", playerID);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

