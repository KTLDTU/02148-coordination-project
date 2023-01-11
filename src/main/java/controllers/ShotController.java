package controllers;

import application.Shot;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;

public class ShotController {

    private final int shotSpeed = 2;
    private Pane gamePane;
    private Queue<Shot> shots;
    private MovementController movementController;
    private Rectangle tractor;

    public ShotController(MovementController movementController, Scene scene) {
        this.movementController = movementController;
        tractor = movementController.tractor;
        gamePane = (Pane) scene.lookup("#gamePane");
        shots = new LinkedList<>();
    }

    public void shoot() {
        if (shots.size() > 5) {
            return;
        }
        Shot shot = new Shot(5.);
        shots.add(shot);

        // Place shot at the center of the tractor
        Bounds bounds = tractor.getBoundsInParent();
        shot.setLayoutX(bounds.getCenterX());
        shot.setLayoutY(bounds.getCenterY());
        shot.setRotate(tractor.getRotate());
        gamePane.getChildren().add(shot);

        shot.setTimer(updateShotTimer(shot));
        shot.getTimer().start();

        // Remove shot after 5s delay
        shot.setDelay(new PauseTransition(Duration.seconds(5)));
        shot.getDelay().setOnFinished(e -> {
            gamePane.getChildren().remove(shot);
            shot.getTimer().stop();
            shots.remove();
        });
        shot.getDelay().play();
    }

    private AnimationTimer updateShotTimer(Shot shot) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateShot(shot);
            }
        };
        return timer;
    }

    public void updateShot(Shot shot) {
        double angle = shot.getRotate() * Math.PI / 180;
        double dX = Math.cos(angle) * shotSpeed;
        double dY = Math.sin(angle) * shotSpeed;
        shot.setLayoutX(shot.getLayoutX() + dX);
        shot.setLayoutY(shot.getLayoutY() + dY);

        // If shot leaves the area of the tractor it is active
        if (!movementController.isCollision(shot, movementController.tractor)) {
            shot.setActive(true);
        }

        // If shot hits a wall it is active
        if (movementController.isWallCollisionHorizontal(shot)) {
            shot.setActive(true);
            shot.setRotate(invertAngleHorizontal(shot.getRotate()));
        }
        if (movementController.isWallCollisionVertical(shot)) {
            shot.setActive(true);
            shot.setRotate(invertAngleVertical(shot.getRotate()));
        }

        // If a shot is active and it hits a tractor, ded
        // TODO: Need some form of list of all tractors, so shot can hit all players, not just the shooter.
        if (movementController.isCollision(shot, movementController.tractor) && shot.isActive()) {
            shot.getDelay().stop();
            // TODO: Remove tractor too.
            gamePane.getChildren().remove(shot);
//            gamePane.getChildren().remove(movementController.tractor);
            shot.getTimer().stop();
            shots.remove(shot);
        }
    }

    private double invertAngleVertical(double angle) {
        return (-1 * angle + 180) % 360;
    }

    private double invertAngleHorizontal(double angle) {
        return (-1 * angle);
    }
}
