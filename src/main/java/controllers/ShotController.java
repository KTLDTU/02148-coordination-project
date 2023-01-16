package controllers;

import application.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.Map;

public class ShotController {

    public final BooleanProperty spacePressed = new SimpleBooleanProperty();
    private static final double SHOT_RADIUS = 4.;
    private static final double SHOT_DISTANCE_FROM_TRACTOR_CENTER = Game.PLAYER_WIDTH / 2 + SHOT_RADIUS;
    private static final int MAX_ACTIVE_SHOTS = 6;
    private static final int SHOT_SPEED = 3;
    private int ownNumShots;
    private Pane gamePane;
    private Game game;

    public ShotController(Game game) {
        this.game = game;
        gamePane = game.gamePane;
        ownNumShots = 0;

        spacePressed.addListener((((observableValue, aBoolean, t1) -> {
            if (!aBoolean) timer.start();
            else timer.stop();
        })));
    }

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long timestamp) {
            if (spacePressed.get() && ownNumShots < MAX_ACTIVE_SHOTS) {
                ownNumShots++;
                spacePressed.set(false);

                // Place shot in front of tractor
                Bounds bounds = game.myTractor.getBoundsInParent();

                double angleInDegrees = game.myTractor.getRotate();
                double angleInRadians = angleInDegrees * Math.PI / 180;
                double x = bounds.getCenterX() + Math.cos(angleInRadians) * SHOT_DISTANCE_FROM_TRACTOR_CENTER;
                double y = bounds.getCenterY() + Math.sin(angleInRadians) * SHOT_DISTANCE_FROM_TRACTOR_CENTER;

                int shotID;

                try {
                    shotID = (int) game.gameSpace.get(new ActualField("shot id"), new FormalField(Integer.class))[1];
                    game.gameSpace.put("shot id", shotID + 1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                new Thread(new ShotBroadcaster(game, game.MY_PLAYER_ID, shotID, x, y, angleInDegrees)).start();
            }
        }
    };

    public Shot shoot(double x, double y, double angleInDegrees, int playerID, int shotID) {
        Shot shot = new Shot(SHOT_RADIUS, playerID, shotID);
        shot.setLayoutX(x);
        shot.setLayoutY(y);
        shot.setRotate(angleInDegrees);
        gamePane.getChildren().add(shot);

        shot.setTimer(updateShotTimer(shot));
        shot.getTimer().start();

        // Remove shot after 5s delay
        shot.setDelay(new PauseTransition(Duration.seconds(5)));
        shot.getDelay().setOnFinished(e -> {
            removeShot(shot);
        });
        shot.getDelay().play();

        return shot;
    }

    public void removeShot(Shot shot) {
        shot.getDelay().stop();
        gamePane.getChildren().remove(shot);
        shot.getTimer().stop();

        if (shot.getPlayerID() == game.MY_PLAYER_ID)
            ownNumShots--;

        game.shots.remove(shot.getShotID());
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
        double dX = Math.cos(angle) * SHOT_SPEED;
        double dY = Math.sin(angle) * SHOT_SPEED;
        shot.setLayoutX(shot.getLayoutX() + dX);
        shot.setLayoutY(shot.getLayoutY() + dY);

        // If shot hits a wall change rotation
        if (game.grid.isWallCollisionHorizontal(shot))
            shot.setRotate(invertAngleHorizontal(shot.getRotate()));
        if (game.grid.isWallCollisionVertical(shot))
            shot.setRotate(invertAngleVertical(shot.getRotate()));

        // If a shot hits a tractor, ded
        if (GameApplication.isHost) {
            for (Map.Entry<Integer, Rectangle> entry : game.tractors.entrySet()) {
                Rectangle tractor = entry.getValue();

                if (game.grid.isCollision(shot, tractor)) {
                    int playerID = entry.getKey();
                    new Thread(new KillBroadcaster(game, playerID, shot.getShotID())).start();
                    break;
                }
            }
        }
    }

    private double invertAngleVertical(double angle) {
        return (-1 * angle + 180) % 360;
    }

    private double invertAngleHorizontal(double angle) {
        return (-1 * angle);
    }
}
