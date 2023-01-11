package controllers;

import application.Game;
import application.Player;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.ArrayList;

public class MovementController {
    private static final int PLAYERS = 2;
    private final BooleanProperty upPressed = new SimpleBooleanProperty();
    private final BooleanProperty downPressed = new SimpleBooleanProperty();
    private final BooleanProperty leftPressed = new SimpleBooleanProperty();
    private final BooleanProperty rightPressed = new SimpleBooleanProperty();
    private final BooleanProperty spacePressed = new SimpleBooleanProperty();

    private final BooleanBinding keyPressed = upPressed.or(downPressed).or(leftPressed).or(rightPressed).or(spacePressed);

    @FXML
    private Rectangle tractor;

    @FXML
    private Scene scene;

    private static final double MOVEMENT_SPEED = 1.9, ROTATION_SPEED = 4.2;
    private final ArrayList<Rectangle> walls;
    private final int PLAYER_ID;
    private final Space gameSpace;

    public MovementController(Player player, Game game) {
        PLAYER_ID = player.PLAYER_ID;
        tractor = player.tractor;
//        tractor = game.tractors.get(PLAYER_ID);
        scene = game.gameScene;
        walls = game.grid.walls;
        gameSpace = game.gameSpace;
        movementSetup();

        keyPressed.addListener(((observableValue, aBoolean, t1) -> {
            if (!aBoolean) timer.start();
            else timer.stop();
        }));

        broadcastPosition();
    }

    private boolean isCollision() {
        for (var wall : walls) {
            Shape intersect = Shape.intersect(tractor, wall);

            if (intersect.getBoundsInParent().getWidth() > 0)
                return true;
        }

        return false;
    }

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long timestamp) {
            if (upPressed.get()) move("forwards");
            if (downPressed.get()) move("backwards");
            if (leftPressed.get()) rotate("counterclockwise");
            if (rightPressed.get()) rotate("clockwise");
        }
    };

    private void move(String dir) {
        double angle = tractor.getRotate() * Math.PI / 180;
        double dX = Math.cos(angle) * MOVEMENT_SPEED * (dir.equals("forwards") ? 1 : -1);
        double dY = Math.sin(angle) * MOVEMENT_SPEED * (dir.equals("forwards") ? 1 : -1);

        tractor.setLayoutX(tractor.getLayoutX() + dX);
        tractor.setLayoutY(tractor.getLayoutY() + dY);

        // naive collision detection - undo movement if colliding with wall
        if (isCollision()) {
            tractor.setLayoutX(tractor.getLayoutX() - dX);
            tractor.setLayoutY(tractor.getLayoutY() - dY);
        }
        else
            broadcastPosition();
    }

    private void rotate(String dir) {
        double dAngle = ROTATION_SPEED * (dir.equals("clockwise") ? 1 : -1);
        tractor.setRotate(tractor.getRotate() + dAngle);

        if (isCollision())
            tractor.setRotate(tractor.getRotate() - dAngle); // undo rotation
        else
            broadcastPosition();
    }

    private void broadcastPosition() {
        try {
            // remove all previous position tuples
            gameSpace.getAll(new ActualField("position"), new ActualField(PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));

            for (int i = 0; i < PLAYERS; i++)
                gameSpace.put("position", PLAYER_ID, tractor.getLayoutX(), tractor.getLayoutY(), tractor.getRotate());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void movementSetup() {
        scene.setOnKeyPressed(e -> setButtonStates(e.getCode(), true));
        scene.setOnKeyReleased(e -> setButtonStates(e.getCode(), false));
    }

    private void setButtonStates(KeyCode key, boolean b) {
        if (key == KeyCode.UP) upPressed.set(b); // TODO: use primitive boolean datatype instead?
        if (key == KeyCode.DOWN) downPressed.set(b);
        if (key == KeyCode.LEFT) leftPressed.set(b);
        if (key == KeyCode.RIGHT) rightPressed.set(b);
        if (key == KeyCode.SPACE) spacePressed.set(b);
    }
}
