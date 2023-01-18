package controllers;

import application.Game;
import javafx.scene.input.KeyCode;

public class InputController {
    Game game;
    MovementController movementController;
    ShotController shotController;

    public InputController(Game game, MovementController movementController, ShotController shotController) {
        this.game = game;
        this.movementController = movementController;
        this.shotController = shotController;
    }

    public void enable() {
        game.gameScene.setOnKeyPressed(e -> setButtonStates(e.getCode(), true));
        game.gameScene.setOnKeyReleased(e -> setButtonStates(e.getCode(), false));
    }

    public void disable() {
        game.gameScene.setOnKeyPressed(e -> {});
        game.gameScene.setOnKeyReleased(e -> {});
    }

    private void setButtonStates(KeyCode key, boolean b) {
        if (key == KeyCode.UP) movementController.upPressed.set(b);
        if (key == KeyCode.DOWN) movementController.downPressed.set(b);
        if (key == KeyCode.LEFT) movementController.leftPressed.set(b);
        if (key == KeyCode.RIGHT) movementController.rightPressed.set(b);
        if (key == KeyCode.SPACE) shotController.spacePressed.set(b);
    }
}
