package controllers;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ShotController {

    private Scene scene;
    private Pane gamePane;
    private Circle shot;
    private MovementController movementController;
    private Rectangle tractor;
    private int maxBounce = 3;
    private int numBounce;
    private int shotSpeed = 2;

    public ShotController(MovementController movementController, Scene scene) {
        this.movementController = movementController;
        tractor = movementController.tractor;
        this.scene = scene;
        gamePane = (Pane) scene.lookup("#gamePane");
    }

    public void shoot() {
        shot = new Circle(5.);
        numBounce = 0;
        // Place shot at the center of the tractor
        Bounds bounds = tractor.getBoundsInParent();
        shot.setLayoutX(bounds.getCenterX());
        shot.setLayoutY(bounds.getCenterY());
        shot.setRotate(tractor.getRotate());
        gamePane.getChildren().add(shot);

        updateShotTimer();
    }

    private void updateShotTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateShot();
            }
        }.start();
    }
    public void updateShot() {
        if (shot == null) {
            return;
        }
        double angle = shot.getRotate() * Math.PI / 180;
        double dX = Math.cos(angle) * shotSpeed;
        double dY = Math.sin(angle) * shotSpeed;

        shot.setLayoutX(shot.getLayoutX() + dX);
        shot.setLayoutY(shot.getLayoutY() + dY);

        if (movementController.isCollision(shot)) {
            // If shot hits a wall it is rotated the opposite direction.
            shot.setRotate(-1 * shot.getRotate());
//            if (numBounce == maxBounce) {
//                gamePane.getChildren().remove(shot);
//            }
            numBounce++;
        }
    }
}
