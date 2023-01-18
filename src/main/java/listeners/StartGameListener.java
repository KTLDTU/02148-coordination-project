package listeners;

import application.GameApplication;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.Space;

public class StartGameListener implements Runnable {

    private Stage stage;
    private Space roomSpace;
    private GameApplication application;

    public StartGameListener(Stage stage, Space roomSpace, GameApplication application) {
        this.stage = stage;
        this.roomSpace = roomSpace;
        this.application = application;
    }

    @Override
    public void run() {
        try {
            roomSpace.query(new ActualField("start game"));
            Platform.runLater(() -> application.launchGame(stage, roomSpace));
        } catch (InterruptedException e) {
        }
    }
}
