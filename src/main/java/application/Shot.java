package application;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.shape.Circle;

public class Shot extends Circle {
    private AnimationTimer timer;
    private PauseTransition delay;
    private int playerID;
    private int shotID;

    public Shot(double v, int playerID, int shotID) {
        super(v);
        this.playerID = playerID;
        this.shotID = shotID;
    }

    public AnimationTimer getTimer() {
        return timer;
    }

    public void setTimer(AnimationTimer timer) {
        this.timer = timer;
    }

    public PauseTransition getDelay() {
        return delay;
    }

    public void setDelay(PauseTransition delay) {
        this.delay = delay;
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getShotID() {
        return shotID;
    }
}
