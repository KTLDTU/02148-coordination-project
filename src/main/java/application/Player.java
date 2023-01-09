package application;

import controllers.MovementController;
import javafx.scene.shape.Rectangle;

public class Player {
    private MovementController movementController;
    public Rectangle tractor;

    public Player(Game game) {
        game.gameController.initializePlayer(game, this);
        movementController = new MovementController(tractor, game);
    }
}
