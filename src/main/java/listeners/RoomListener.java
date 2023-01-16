package listeners;

import controllers.RoomSceneViewController;
import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.ArrayList;

public class RoomListener implements Runnable {
    Space space;
    RoomSceneViewController roomController;
    ArrayList<String> playerNames = new ArrayList<>();

    public RoomListener(Space space, RoomSceneViewController roomController) {
        this.space = space;
        this.roomController = roomController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ArrayList<String> newPlayerNames = (ArrayList<String>) space.query(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];

                // Update list of player names if the two lists are different
                if (!playerNames.equals(newPlayerNames)) {
                    playerNames = newPlayerNames;
                    Platform.runLater(() -> {
                        roomController.updatePlayerList(newPlayerNames);
                    });
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
