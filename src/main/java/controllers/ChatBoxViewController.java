package controllers;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;

public class ChatBoxViewController {
    // Not used atm
    private ArrayList<Label> messages = new ArrayList<>();

    private VBox chatBox;
    int index = 0;

    @FXML
    private TextArea area;
    @FXML
    private ScrollPane container;

    void postMessage(String message) {
        // HBox to hold messages
        HBox messageBox = new HBox();
        messageBox.setPrefWidth(200);
        // Get the message from the text area and convert to label with desired properties.
        if (message.trim().isEmpty()) {
            return;
        }
        Label textLabel = new Label(message);
        textLabel.setPrefWidth(175);
        textLabel.setWrapText(true);
        textLabel.setPadding(new Insets(0, 2, 0,0));
        messageBox.getChildren().add(textLabel);
        messages.add(textLabel);

        // Position of the message should change depending on who the message is from
        // TODO: Extend this to work with users in a jSpace
        if (index % 2 == 0) {
            messageBox.setStyle("-fx-background-color:#d7d7d7");
            messageBox.setAlignment(Pos.CENTER_LEFT);
            textLabel.setTextAlignment(TextAlignment.LEFT);
            textLabel.setAlignment(Pos.CENTER_LEFT);
        } else {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            textLabel.setTextAlignment(TextAlignment.RIGHT);
            textLabel.setAlignment(Pos.CENTER_RIGHT);
        }
        chatBox.getChildren().add(messageBox);
        index++;
    }

    // Listener applied to the TextArea that sends the message if the enter key is pressed
    // If enter is pressed while shift is held down a newline is appended instead.
    @FXML
    void sendOnEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.getKeyCode("Enter")) {
            if (!event.isShiftDown()) {
                postMessage(area.getText());
                area.clear();
            } else {
                area.appendText("\n");
            }
        }
    }

    @FXML
    void initialize() {
        assert area != null : "fx:id=\"area\" was not injected: check your FXML file 'ChatboxView.fxml'.";
        assert container != null : "fx:id=\"container\" was not injected: check your FXML file 'ChatboxView.fxml'.";
        chatBox = new VBox();
        // Ensure scrollbar is always scrolled all the way down
        chatBox.heightProperty().addListener(observable -> container.setVvalue(1.0));
        container.setContent(chatBox);
    }
}
