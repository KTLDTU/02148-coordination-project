package listeners;

import application.Room;
import controllers.RoomSceneViewController;
import datatypes.ArrayListInt;
import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.ArrayList;

public class RoomPlayerListener implements Runnable {
    Space roomSpace;
    RoomSceneViewController roomController;
    ArrayList<String> playerNames = new ArrayList<>();
    ArrayListInt playerIds = new ArrayListInt();

    public RoomPlayerListener(Space roomSpace, RoomSceneViewController roomController) {
        this.roomSpace = roomSpace;
        this.roomController = roomController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ArrayList<String> newPlayerNames = (ArrayList<String>) roomSpace.query(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];

                // Update list of player names if the two lists are different
                if (!playerNames.equals(newPlayerNames)) {
                    playerNames = newPlayerNames;
                    Platform.runLater(() -> roomController.updatePlayerList(newPlayerNames));
                    Room.playerNames = newPlayerNames;
                }
                ArrayListInt newPlayerIds = (ArrayListInt) roomSpace.query(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];

                if (!playerIds.equals(newPlayerIds)) {
                    roomSpace.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
                    playerIds = newPlayerIds;
                    roomSpace.put("playerIdList", playerIds);
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
