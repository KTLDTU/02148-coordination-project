package application;

import controllers.GameSceneController;
import controllers.MovementController;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import org.jspace.ActualField;
import org.jspace.FormalField;
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
        movementController = new MovementController(gameController);
    }

    @Override
    public void run() {
        try {
            String uri = "tcp://" + Game.HOST_IP + ":9005/game?keep";
            game = new RemoteSpace(uri);
            game.put("join");
            System.out.println("Player has put \"join\" in remote space");
            Grid grid = (Grid) game.get(new ActualField("gameGrid"), new FormalField(Grid.class))[1];
            gameController.setGrid(grid);

            Rectangle player = gameController.initializePlayer();
            movementController.makeMovable(player, gameScene);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
