package controllers;

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

    private Scene scene;
    private Pane gamePane;
    private Queue<Circle> shots;
    private MovementController movementController;
    private Rectangle tractor;
    private int shotSpeed = 2;

    public ShotController(MovementController movementController, Scene scene) {
        this.movementController = movementController;
        tractor = movementController.tractor;
        this.scene = scene;
        gamePane = (Pane) scene.lookup("#gamePane");
        shots = new LinkedList<>();
    }

    public void shoot() {
        if (shots.size() > 5) {
            return;
        }
        Circle shot = new Circle(5.);
        shots.add(shot);
        // Place shot at the center of the tractor
        Bounds bounds = tractor.getBoundsInParent();
        shot.setLayoutX(bounds.getCenterX());
        shot.setLayoutY(bounds.getCenterY());
        shot.setRotate(tractor.getRotate());

        gamePane.getChildren().add(shot);

        AnimationTimer timer = updateShotTimer(shot);
        timer.start();

        // Remove shot after 5s delay
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> {
            gamePane.getChildren().remove(shot);
            timer.stop();
            shots.remove();
        });
        delay.play();
    }

    private AnimationTimer updateShotTimer(Circle shot) {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateShot(shot);
            }
        };
        return timer;
    }

    public void updateShot(Circle shot) {
        double angle = shot.getRotate() * Math.PI / 180;
        double dX = Math.cos(angle) * shotSpeed;
        double dY = Math.sin(angle) * shotSpeed;
        System.out.println("dX: " + dX + " dY: " + dY);
        shot.setLayoutX(shot.getLayoutX() + dX);
        shot.setLayoutY(shot.getLayoutY() + dY);

        if (movementController.isCollisionHorizontal(shot)) {
            shot.setRotate(invertAngleHorizontal(shot.getRotate()));
        }
        if (movementController.isCollisionVertical(shot)) {
            shot.setRotate(invertAngleVertical(shot.getRotate()));
        }
    }

    private double invertAngleVertical(double angle) {
        return (-1 * angle + 180) % 360;
    }

    private double invertAngleHorizontal(double angle) {
        return (-1 * angle);
    }
}
