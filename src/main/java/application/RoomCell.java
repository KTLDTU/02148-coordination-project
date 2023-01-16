package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class RoomCell extends ListCell<Room> {

    @FXML
    private Label ipAddress;

    @FXML
    private Label roomId;

    @FXML
    private Label roomName;

    public RoomCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/room-cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Room item, boolean empty) {
        super.updateItem(item, empty);

        if(empty || item == null) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
        else {
            roomName.setText(item.getName());
            ipAddress.setText(item.getIp());
            int roomIdNumber = item.getRoomId();
            roomId.setText(Integer.toString(roomIdNumber));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}