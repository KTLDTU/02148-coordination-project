package application;

import controllers.GameSceneController;
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
    public InputListener inputListener;
    public static List<Color> colors = new ArrayList<>(Arrays.asList(Color.YELLOWGREEN, Color.RED, Color.GREEN, Color.BLUE));
    public String[] imageURL = new String[]{"/yellow.png", "/red.png", "/green.png", "/blue.png"};
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

            if (GameApplication.isHost) {
                gameSpace.put("shot id", 0);
            }
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
        MovementController movementController = new MovementController(this);
        shotController = new ShotController(this);
        inputListener = new InputListener(this, movementController, shotController);

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
        try {
            if (movementListener != null) {
                signalGameEndToThreads();
                joinAllThreads();
                waitForRunLater();
                synchronizePlayers();
            }

            gameController.displayPlayersNameAndScore(playersIdNameMap, playerScores);
            Platform.runLater(() -> gamePane.getChildren().clear());
            tractors = new HashMap<>();

            synchronized (shotsLock) {
                shots = new HashMap<>();
            }

            if (GameApplication.isHost) {
                Grid grid = new Grid(gamePane);

                for (int playerID : playersIdNameMap.keySet())
                    gameSpace.put("connected squares", playerID, grid.connectedSquares);
            }

            HashSetIntArray connectedSquares = (HashSetIntArray) gameSpace.get(new ActualField("connected squares"), new ActualField(MY_PLAYER_ID), new FormalField(HashSetIntArray.class))[2];
            setGrid(connectedSquares);
            spawnPlayers();

            Thread playerPositionBroadcaster = new Thread(new PlayerPositionBroadcaster(this));
            playerPositionBroadcaster.start();
            playerPositionBroadcaster.join();

            synchronizePlayers();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void signalGameEndToThreads() {
        try {
            gameSpace.put("player position", -1, MY_PLAYER_ID, -1.0, -1.0, -1.0);
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

                if (playerID == -1)
                    break;

                Rectangle tractor = game.tractors.get(playerID);

                Platform.runLater(() -> {
                    if (tractor != null) {
                        tractor.setLayoutX(tractorX);
                        tractor.setLayoutY(tractorY);
                        tractor.setRotate(tractorRot);
                    }
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

                if (playerID == -1)
                    break;

                Shot shot;

                synchronized (game.shotsLock) {
                    shot = game.shotController.shoot(shotX, shotY, shotRot, playerID, shotID);
                    game.shots.put(shotID, shot);
                }

                // if a player shoots directly into a wall, they die immediately
                if (GameApplication.isHost && game.grid.isWallCollision(shot)) {
                    Thread killBroadcaster = new Thread(new KillBroadcaster(game, playerID, shotID));
                    killBroadcaster.start();
                    killBroadcaster.join();
                }
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

                if (playerID == -1)
                    break;

                synchronized (game.shotsLock) {
                    Shot shot = game.shots.get(shotID);

                    if (shot != null)
                        game.shotController.removeShot(shot);
                }

                Rectangle tractor = game.tractors.get(playerID);

                if (tractor != null) {
                    Platform.runLater(() -> game.gamePane.getChildren().remove(tractor));
                    game.waitForRunLater();
                    game.tractors.remove(playerID);

                    if (GameApplication.isHost && game.numPlayersAlive() == 1)
                        new Thread(new GameEndTimer(game)).start();

                    if (playerID == game.MY_PLAYER_ID)
                        game.inputListener.disable();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

class GameEndListener implements Runnable {
    private Game game;

    public GameEndListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            game.gameSpace.get(new ActualField("game end"), new ActualField(game.MY_PLAYER_ID));

            synchronized (game.shotsLock) {
                while (!game.shots.isEmpty()) {
                    Shot shot = (Shot) game.shots.values().toArray()[0];

                    if (shot != null)
                        game.shotController.removeShot(shot);
                    else
                        game.shots.remove(shot.getShotID());
                }
            }

            Integer winnerPlayerID = (game.tractors.isEmpty() ? null : (Integer) game.tractors.keySet().toArray()[0]);
            game.incrementPlayerScore(winnerPlayerID);
            game.newRound();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
