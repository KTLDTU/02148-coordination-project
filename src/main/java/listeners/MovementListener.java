package listeners;

import application.Game;
import controllers.MovementController;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.HashMap;

public class MovementListener implements Runnable {
    private Game game;
    private HashMap<Integer, Integer> keysPressed;
    private HashMap<Integer, AnimationTimer> timers;
    private HashMap<Integer, Long> lastBroadcast;

    public MovementListener(Game game) {
        this.game = game;
        keysPressed = new HashMap<>();
        timers = new HashMap<>();
        lastBroadcast = new HashMap<>();

        // create animation timer for all enemy tractors
        for (Integer playerID : game.playersIdNameMap.keySet()) {
            if (playerID == game.MY_PLAYER_ID)
                continue;

            keysPressed.put(playerID, 0);
            lastBroadcast.put(playerID, System.currentTimeMillis());

            timers.put(playerID, new AnimationTimer() {
                @Override
                public void handle(long l) {
                    if (System.currentTimeMillis() - lastBroadcast.get(playerID) < MovementController.MAX_DELAY * 2) {
                        int curKeysPressed = keysPressed.get(playerID);
                        Rectangle tractor = game.tractors.get(playerID);

                        if (tractor != null) {
                            if ((curKeysPressed & (1 << 0)) > 0) game.movementController.move(tractor, "forwards");
                            if ((curKeysPressed & (1 << 1)) > 0) game.movementController.move(tractor, "backwards");
                            if ((curKeysPressed & (1 << 2)) > 0)
                                game.movementController.rotate(tractor, "counterclockwise");
                            if ((curKeysPressed & (1 << 3)) > 0) game.movementController.rotate(tractor, "clockwise");
                        }
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("player position"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Integer.class));
                int playerID = (int) obj[1];
                double tractorX = (double) obj[3];
                double tractorY = (double) obj[4];
                double tractorRot = (double) obj[5];
                int curKeysPressed = (int) obj[6];
                keysPressed.replace(playerID, curKeysPressed);

                if (playerID == -1)
                    break;

                Rectangle tractor = game.tractors.get(playerID);

                Platform.runLater(() -> {
                    if (tractor != null) {
                        tractor.setLayoutX(tractorX);
                        tractor.setLayoutY(tractorY);
                        tractor.setRotate(tractorRot);

                        AnimationTimer timer = timers.get(playerID);
                        lastBroadcast.replace(playerID, System.currentTimeMillis());

                        if (game.movementPrediction && curKeysPressed > 0)
                            timer.start();
                        else
                            timers.get(playerID).stop();
                    }
                });
            }

            for (AnimationTimer timer : timers.values())
                timer.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
