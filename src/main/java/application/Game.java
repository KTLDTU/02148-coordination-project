package application;

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

    public Game(Stage stage, Space gameSpace, Map<Integer, String> playersIdNameMap, int MY_PLAYER_ID) {
        try {
            this.gameSpace = gameSpace;
            this.MY_PLAYER_ID = MY_PLAYER_ID;
            this.playersIdNameMap = playersIdNameMap;

            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gameScene = new Scene(scene);
            stage.setScene(gameScene);
            tractors = new HashMap<>();
            shots = new HashMap<>();
            gamePane = gameController.gamePane;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeGrid() {
        grid = new Grid(gamePane);
        gameController.displayGrid(grid);
    }

    public void setGrid(HashSetIntArray connectedSquares) {
        grid = new Grid(gamePane, connectedSquares);
        gameController.displayGrid(grid);
    }

    public void spawnPlayers() {
        for (Integer playerID : playersIdNameMap.keySet()) {
            Rectangle newTractor = (playerID == MY_PLAYER_ID ? randomSpawn() : new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT));
            tractors.put(playerID, newTractor);
            gamePane.getChildren().add(tractors.get(playerID));
        }

        myTractor = tractors.get(MY_PLAYER_ID);
        MovementController movementController = new MovementController(this);
        shotController = new ShotController(this);
        new InputListener(this, movementController, shotController);

        Thread movementListener = new Thread(new MovementListener(this));
        movementListener.setDaemon(true);
        movementListener.start();

        Thread shotListener = new Thread((new ShotListener(this)));
        shotListener.setDaemon(true);
        shotListener.start();

        Thread killListener = new Thread((new KillListener(this)));
        killListener.setDaemon(true);
        killListener.start();

        if (GameApplication.isHost) {
            try {
                gameSpace.put("shot id", 0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        gameController.initializePlayerNames(playersIdNameMap.values());
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
                Object[] obj = game.gameSpace.get(new ActualField("player position"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];
                double tractorX = (double) obj[3];
                double tractorY = (double) obj[4];
                double tractorRot = (double) obj[5];

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

class ShotListener implements Runnable {
    private Game game;

    public ShotListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("new shot"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];
                int shotID = (int) obj[3];
                double shotX = (double) obj[4];
                double shotY = (double) obj[5];
                double shotRot = (double) obj[6];

                Platform.runLater(() -> {
                    Shot shot = game.shotController.shoot(shotX, shotY, shotRot, playerID, shotID);
                    game.shots.put(shotID, shot);

                    // if a player shoots directly into a wall, they die immediately
                    if (GameApplication.isHost && game.grid.isWallCollision(shot)) {
                        new Thread(new KillBroadcaster(game, playerID, shotID)).start();
                    }
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class KillListener implements Runnable {
    private Game game;

    public KillListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("kill"), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Integer.class));
                int playerID = (int) obj[2];
                int shotID = (int) obj[3];

                Platform.runLater(() -> {
                    game.shotController.removeShot(game.shots.get(shotID));
                    game.gamePane.getChildren().remove(game.tractors.get(playerID));
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
