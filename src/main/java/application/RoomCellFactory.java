package application;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class RoomCellFactory implements Callback<ListView<Room>, ListCell<Room>> {

    @Override
    public ListCell<Room> call(ListView<Room> param) {
        return new RoomCell();
    }
}