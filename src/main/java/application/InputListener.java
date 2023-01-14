package application;

import controllers.MovementController;
import controllers.ShotController;
import javafx.scene.input.KeyCode;

public class InputListener {
    Game game;
    MovementController movementController;
    ShotController shotController;

    public InputListener(Game game, MovementController movementController, ShotController shotController) {
        this.game = game;
        this.movementController = movementController;
        this.shotController = shotController;

        game.gameScene.setOnKeyPressed(e -> setButtonStates(e.getCode(), true));
        game.gameScene.setOnKeyReleased(e -> setButtonStates(e.getCode(), false));
    }

    private void setButtonStates(KeyCode key, boolean b) {
        if (key == KeyCode.UP) movementController.upPressed.set(b);
        if (key == KeyCode.DOWN) movementController.downPressed.set(b);
        if (key == KeyCode.LEFT) movementController.leftPressed.set(b);
        if (key == KeyCode.RIGHT) movementController.rightPressed.set(b);
        if (key == KeyCode.SPACE) shotController.spacePressed.set(b);
    }
}
