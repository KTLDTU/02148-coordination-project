package application;

import controllers.MovementController;
import javafx.scene.shape.Rectangle;

public class Player {
    public static final double PLAYER_WIDTH = 20, PLAYER_HEIGHT = 15;
    private MovementController movementController;
    public Rectangle tractor;
    public final int PLAYER_ID;

    public Player(Game game, int id) {
        PLAYER_ID = id;
        game.gameController.initializePlayer(game, this);
        movementController = new MovementController(this, game);
    }
}
