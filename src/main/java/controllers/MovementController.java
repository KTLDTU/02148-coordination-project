package controllers;

import application.PlayerPositionBroadcaster;
import application.Game;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.shape.Rectangle;

public class MovementController {
    public static final int MAX_DELAY = 50;
    public final BooleanProperty upPressed = new SimpleBooleanProperty();
    public final BooleanProperty downPressed = new SimpleBooleanProperty();
    public final BooleanProperty leftPressed = new SimpleBooleanProperty();
    public final BooleanProperty rightPressed = new SimpleBooleanProperty();

    private final BooleanBinding keyPressed = upPressed.or(downPressed).or(leftPressed).or(rightPressed);

    public static final double MOVEMENT_SPEED = 1.9, ROTATION_SPEED = 4.2;
    private final Game game;
    public final Rectangle tractor;
    private Long lastBroadcast;

    public MovementController(Game game) {
        this.game = game;
        this.tractor = game.myTractor;

        keyPressed.addListener(((observableValue, aBoolean, t1) -> {
            if (!aBoolean) timer.start();
            else timer.stop();
        }));

        lastBroadcast = System.currentTimeMillis();
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
        if (game.grid.isWallCollision(tractor)) {
            tractor.setLayoutX(tractor.getLayoutX() - dX);
            tractor.setLayoutY(tractor.getLayoutY() - dY);
        } else if (getLastBroadcastTime() > MAX_DELAY) {
            lastBroadcast = System.currentTimeMillis();
            new Thread(new PlayerPositionBroadcaster(game, this)).start();
        }
    }

    private void rotate(String dir) {
        double dAngle = ROTATION_SPEED * (dir.equals("clockwise") ? 1 : -1);
        tractor.setRotate(tractor.getRotate() + dAngle);

        if (game.grid.isWallCollision(tractor)) {
            tractor.setRotate(tractor.getRotate() - dAngle); // undo rotation
        } else if (getLastBroadcastTime() > MAX_DELAY) {
            lastBroadcast = System.currentTimeMillis();
            new Thread(new PlayerPositionBroadcaster(game, this)).start();
        }
    }

    private long getLastBroadcastTime() {
        long time = System.currentTimeMillis();
        long timeDiff = time - lastBroadcast;
        return timeDiff;
    }
}
