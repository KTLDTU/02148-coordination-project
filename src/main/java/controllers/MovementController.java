package controllers;

import application.Broadcaster;
import application.Game;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class MovementController {
    public static final int MAX_DELAY = 25;
    private final BooleanProperty upPressed = new SimpleBooleanProperty();
    private final BooleanProperty downPressed = new SimpleBooleanProperty();
    private final BooleanProperty leftPressed = new SimpleBooleanProperty();
    private final BooleanProperty rightPressed = new SimpleBooleanProperty();
    private final BooleanProperty spacePressed = new SimpleBooleanProperty();

    private final BooleanBinding keyPressed = upPressed.or(downPressed).or(leftPressed).or(rightPressed).or(spacePressed);

    private static final double MOVEMENT_SPEED = 1.9, ROTATION_SPEED = 4.2;
    private final Game game;
    private final Rectangle tractor;

    private Long lastBroadcast;

    public MovementController(Game game) {
        this.game = game;
        this.tractor = game.myTractor;
        movementSetup();

        keyPressed.addListener(((observableValue, aBoolean, t1) -> {
            if (!aBoolean) timer.start();
            else timer.stop();
        }));
        new Thread(new Broadcaster(game)).start();
        lastBroadcast = System.currentTimeMillis();
    }

    private boolean isCollision() {
        for (var wall : game.grid.walls) {
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
        } else if (getLastBroadcastTime() > MAX_DELAY) {
            lastBroadcast = System.currentTimeMillis();
            new Thread(new Broadcaster(game)).start();
        }
    }

    private void rotate(String dir) {
        double dAngle = ROTATION_SPEED * (dir.equals("clockwise") ? 1 : -1);
        tractor.setRotate(tractor.getRotate() + dAngle);

        if (isCollision()) {
            tractor.setRotate(tractor.getRotate() - dAngle); // undo rotation
        } else if (getLastBroadcastTime() > MAX_DELAY) {
            lastBroadcast = System.currentTimeMillis();
            new Thread(new Broadcaster(game)).start();
        }
    }


    private void movementSetup() {
        game.gameScene.setOnKeyPressed(e -> setButtonStates(e.getCode(), true));
        game.gameScene.setOnKeyReleased(e -> setButtonStates(e.getCode(), false));
    }

    private void setButtonStates(KeyCode key, boolean b) {
        if (key == KeyCode.UP) upPressed.set(b); // TODO: use primitive boolean datatype instead?
        if (key == KeyCode.DOWN) downPressed.set(b);
        if (key == KeyCode.LEFT) leftPressed.set(b);
        if (key == KeyCode.RIGHT) rightPressed.set(b);
        if (key == KeyCode.SPACE) spacePressed.set(b);
    }

    private long getLastBroadcastTime() {
        long time = System.currentTimeMillis();
        long timeDiff = time - lastBroadcast;
        return timeDiff;
    }
}
