package application;

import controllers.GameSceneController;
import controllers.MovementController;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import org.jspace.RemoteSpace;

import java.io.IOException;

public class Player implements Runnable {
    private MovementController movementController;
    private RemoteSpace game;
    private GameSceneController gameController;
    private Scene gameScene;

    public Player(GameSceneController gameController, Scene gameScene) {
        this.gameController = gameController;
        this.gameScene = gameScene;
        Rectangle player = gameController.initializePlayer();
        movementController = new MovementController(gameController);
        movementController.makeMovable(player, gameScene);
    }

    @Override
    public void run() {
        try {
            String uri = "tcp://" + Game.HOST_IP + ":9005/game?keep";
            game = new RemoteSpace(uri);
            game.put("join");
            System.out.println("Player has put \"join\" in remote space");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
