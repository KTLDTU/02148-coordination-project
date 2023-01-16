package listeners;

import application.GameApplication;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.Space;

public class StartGameListener implements Runnable {

    private Stage stage;
    private Space space;
    private GameApplication application;

    public StartGameListener(Stage stage, Space space, GameApplication application) {
        this.stage = stage;
        this.space = space;
        this.application = application;
    }

    @Override
    public void run() {
        try {
            space.get(new ActualField("start game"));
            Platform.runLater(() -> application.launchGame(stage));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
