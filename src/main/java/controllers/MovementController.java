package controllers;

import application.Game;
import application.Grid;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

public class MovementController {
    private final BooleanProperty upPressed = new SimpleBooleanProperty();
    private final BooleanProperty downPressed = new SimpleBooleanProperty();
    private final BooleanProperty leftPressed = new SimpleBooleanProperty();
    private final BooleanProperty rightPressed = new SimpleBooleanProperty();
    private final BooleanProperty spacePressed = new SimpleBooleanProperty();
    private ShotController shotController;

    private final BooleanBinding keyPressed = upPressed.or(downPressed).or(leftPressed).or(rightPressed).or(spacePressed);

    @FXML
    public Rectangle tractor;

    @FXML
    private Scene scene;

    public static final double MOVEMENT_SPEED = 1.9, ROTATION_SPEED = 4.2;
    private Grid grid;

    public MovementController(Rectangle tractor, Game game) {
        this.tractor = tractor;
        this.scene = game.gameScene;
        this.grid = game.grid;
        shotController = new ShotController(this, scene);
        movementSetup();

        keyPressed.addListener(((observableValue, aBoolean, t1) -> {
            if (!aBoolean) timer.start();
            else timer.stop();
        }));
    }

    public boolean isCollision(Shape shape) {
        return isCollisionHorizontal(shape) || isCollisionVertical(shape);
    }

    public boolean isCollisionHorizontal(Shape shape) {
        for (var wall : grid.horizontalWalls) {
            Shape intersect = Shape.intersect(shape, wall);

            if (intersect.getBoundsInParent().getWidth() > 0)
                return true;
        }

        return false;
    }

    public boolean isCollisionVertical(Shape shape) {
        for (var wall : grid.verticalWalls) {
            Shape intersect = Shape.intersect(shape, wall);

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
            if (spacePressed.get()) {
                spacePressed.set(false);
                shotController.shoot();
            }
        }
    };

    private void move(String dir) {
        double angle = tractor.getRotate() * Math.PI / 180;
        double dX = Math.cos(angle) * MOVEMENT_SPEED * (dir.equals("forwards") ? 1 : -1);
        double dY = Math.sin(angle) * MOVEMENT_SPEED * (dir.equals("forwards") ? 1 : -1);

        tractor.setLayoutX(tractor.getLayoutX() + dX);
        tractor.setLayoutY(tractor.getLayoutY() + dY);

        // naive collision detection - undo movement if colliding with wall
        if (isCollision(tractor)) {
            tractor.setLayoutX(tractor.getLayoutX() - dX);
            tractor.setLayoutY(tractor.getLayoutY() - dY);
        }
    }

    private void rotate(String dir) {
        double dAngle = ROTATION_SPEED * (dir.equals("clockwise") ? 1 : -1);
        tractor.setRotate(tractor.getRotate() + dAngle);

        if (isCollision(tractor))
            tractor.setRotate(tractor.getRotate() - dAngle); // undo rotation
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
