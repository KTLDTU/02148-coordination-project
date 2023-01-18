package listeners;

import application.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

public class RoomListListener implements Runnable {

    private Space lobbySpace;

    private ArrayList<Room> roomList = new ArrayList<>();
    private ListView<Room> roomListView;

    public RoomListListener(Space lobbySpace, ListView<Room> roomListView) {
        this.lobbySpace = lobbySpace;
        this.roomListView = roomListView;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Query all the rooms and create a new list of rooms
                List<Object[]> newRoomObjects = lobbySpace.queryAll(new ActualField("room"), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
                ArrayList<Room> newRoomList = new ArrayList<>();
                for (Object[] objects : newRoomObjects) {
                    newRoomList.add(new Room((String) objects[1], (String) objects[2], (Integer) objects[3]));
                }
                // Check if the old roomList and newRoomList differ, if so update the ListView
                if (!(roomList.containsAll(newRoomList) && newRoomList.containsAll(roomList))) {
                    roomList = newRoomList;
                    Platform.runLater(() -> roomListView.setItems(FXCollections.observableArrayList(roomList)));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
