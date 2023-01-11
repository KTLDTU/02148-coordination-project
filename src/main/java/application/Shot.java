package application;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.shape.Circle;

public class Shot extends Circle {
    private boolean isActive;
    private AnimationTimer timer;
    private PauseTransition delay;

    public Shot(double v) {
        super(v);
        isActive = false;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
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
}
