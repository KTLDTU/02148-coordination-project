package application;

import controllers.MovementController;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class PlayerPositionBroadcaster implements Runnable {
    private Game game;
    private MovementController movementController;

    public PlayerPositionBroadcaster(Game game, MovementController movementController) {
        this.game = game;
        this.movementController = movementController;
    }

    @Override
    public void run() {
        try {
            // remove all previous position tuples
            game.gameSpace.getAll(new ActualField("player position"), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Integer.class));

            // create bitmask for keys pressed ... up, down, left, right
            int keysPressed = 0;
            if (movementController.upPressed.get()) keysPressed |= (1 << 0);
            if (movementController.downPressed.get()) keysPressed |= (1 << 1);
            if (movementController.leftPressed.get()) keysPressed |= (1 << 2);
            if (movementController.rightPressed.get()) keysPressed |= (1 << 3);

            for (int playerID : game.playersIdNameMap.keySet())
                if (playerID != game.MY_PLAYER_ID)
                    game.gameSpace.put("player position", game.MY_PLAYER_ID, playerID, game.myTractor.getLayoutX(), game.myTractor.getLayoutY(), game.myTractor.getRotate(), keysPressed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
