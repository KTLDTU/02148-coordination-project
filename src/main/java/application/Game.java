package application;

import broadcasters.PlayerPositionBroadcaster;
import controllers.GameSceneController;
import controllers.InputController;
import controllers.MovementController;
import controllers.ShotController;
import datatypes.HashSetIntArray;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import listeners.GameEndListener;
import listeners.KillListener;
import listeners.MovementListener;
import listeners.ShotListener;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Game {
    public static final double PLAYER_WIDTH = 20, PLAYER_HEIGHT = 15;
    public final int MY_PLAYER_ID;
    public GameSceneController gameController;
    public Scene gameScene;
    public Pane gamePane;
    public Grid grid;
    public Space gameSpace;
    public HashMap<Integer, Rectangle> tractors;
    public HashMap<Integer, Integer> playerScores;
    public Rectangle myTractor;
    public Map<Integer, String> playersIdNameMap;
    public ShotController shotController;
    public HashMap<Integer, Shot> shots;
    public final Object shotsLock = new Object();
    public InputController inputController;
    public static List<Color> colors = new ArrayList<>(Arrays.asList(Color.YELLOWGREEN, Color.RED, Color.GREEN, Color.BLUE));
    public String[] imageURL = new String[]{"/yellow.png", "/red.png", "/green.png", "/blue.png"};
    public boolean movementPrediction = true;
    public MovementController movementController;
    public Thread movementListener, shotListener, killListener;

    public Game(Stage stage, Space gameSpace, Map<Integer, String> playersIdNameMap, int MY_PLAYER_ID) {
        try {
            this.gameSpace = gameSpace;
            this.MY_PLAYER_ID = MY_PLAYER_ID;
            this.playersIdNameMap = playersIdNameMap;
            movementListener = shotListener = killListener = null;

            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/game-scene-view.fxml"));
            BorderPane scene = gameLoader.load();
            gameController = gameLoader.getController();
            gamePane = gameController.gamePane;
            gameScene = new Scene(scene);
            stage.setScene(gameScene);
            playerScores = new HashMap<>();

            for (Integer playerID : playersIdNameMap.keySet()) {
                playerScores.put(playerID, 0);
            }

            if (GameApplication.isRoomHost) {
                gameSpace.put("shot id", 0);
            }

            gameController.movementPredictionOn.setOnMouseReleased(e -> movementPrediction = true);
            gameController.movementPredictionOff.setOnMouseReleased(e -> movementPrediction = false);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void setGrid(HashSetIntArray connectedSquares) {
        grid = new Grid(gamePane, connectedSquares);
        Platform.runLater(() -> gameController.displayGrid(grid));
    }

    public void spawnPlayers() {
        int index = 0;

        for (Integer playerID : playersIdNameMap.keySet()) {
            Rectangle newTractor = (playerID == MY_PLAYER_ID ? randomSpawn() : new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT));
            tractors.put(playerID, newTractor);
            Image img = new Image(imageURL[index++]);
            newTractor.setFill(new ImagePattern(img));
            Platform.runLater(() -> gamePane.getChildren().add(tractors.get(playerID)));
        }

        myTractor = tractors.get(MY_PLAYER_ID);
        movementController = new MovementController(this);
        shotController = new ShotController(this);
        inputController = new InputController(this, movementController, shotController);

        movementListener = new Thread(new MovementListener(this));
        movementListener.setDaemon(true);
        movementListener.start();

        shotListener = new Thread(new ShotListener(this));
        shotListener.setDaemon(true);
        shotListener.start();

        killListener = new Thread(new KillListener(this));
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
        return tractor;
    }

    public void incrementPlayerScore(Integer playerId) {
        if (playerId != null) {
            playerScores.replace(playerId, playerScores.get(playerId) + 1);
        }
    }

    public int numPlayersAlive() {
        return tractors.size();
    }

    public void newRound() {
        System.out.println("Entered newRound()...");

        try {
            if (movementListener != null) {
                System.out.println("Disabling inputController and stopping movementController timer...");
                inputController.disable();
                movementController.timer.stop();
                System.out.println("Signalling threads...");
                signalGameEndToThreads();
                System.out.println("Joining threads...");
                joinAllThreads();
                System.out.println("Consuming space...");
                consumeEverythingInSpace();
                System.out.println("Waiting for runLater...");
                waitForRunLater();
                System.out.println("Synchronizing players...");
                synchronizePlayers();
            }

            System.out.println("Got past all synchronization stuff...");
            gameController.displayPlayersNameAndScore(playersIdNameMap, playerScores);
            Platform.runLater(() -> gamePane.getChildren().clear());
            tractors = new HashMap<>();

            synchronized (shotsLock) {
                shots = new HashMap<>();
            }

            if (GameApplication.isRoomHost) {
                Grid grid = new Grid(gamePane);
                for (int playerID : playersIdNameMap.keySet())
                    gameSpace.put("connected squares", playerID, grid.connectedSquares);
            }

            HashSetIntArray connectedSquares = (HashSetIntArray) gameSpace.get(new ActualField("connected squares"), new ActualField(MY_PLAYER_ID), new FormalField(HashSetIntArray.class))[2];
            setGrid(connectedSquares);
            spawnPlayers();

            Thread playerPositionBroadcaster = new Thread(new PlayerPositionBroadcaster(this, movementController));
            playerPositionBroadcaster.start();
            playerPositionBroadcaster.join();

            System.out.println("Synchronizing players (end of newRound() function)...");
            synchronizePlayers();
            System.out.println("Enabling inputController...");
            inputController.enable();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void consumeEverythingInSpace() {
        try {
            gameSpace.getAll(new ActualField("player position"), new FormalField(Integer.class), new ActualField(MY_PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Integer.class));
            gameSpace.getAll(new ActualField("new shot"), new FormalField(Integer.class), new ActualField(MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
            gameSpace.getAll(new ActualField("kill"), new ActualField(MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Integer.class));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void signalGameEndToThreads() {
        try {
            gameSpace.put("player position", -1, MY_PLAYER_ID, -1.0, -1.0, -1.0, -1);
            gameSpace.put("new shot", -1, MY_PLAYER_ID, -1, -1.0, -1.0, -1.0);
            gameSpace.put("kill", MY_PLAYER_ID, -1, -1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void joinAllThreads() {
        try {
            movementListener.join();
            shotListener.join();
            killListener.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForRunLater() {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void synchronizePlayers() {
        try {
            for (int i = 0; i < playersIdNameMap.size(); i++)
                gameSpace.put("player ready", MY_PLAYER_ID);

            for (Integer playerID : playersIdNameMap.keySet())
                gameSpace.get(new ActualField("player ready"), new ActualField(playerID));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

