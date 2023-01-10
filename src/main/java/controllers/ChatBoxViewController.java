package controllers;

import application.ChatClient;
import application.ChatHost;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

public class ChatBoxViewController {
    ChatHost chatHost = new ChatHost(null, "Jonas");
    // Not used atm
    private ArrayList<Label> messages = new ArrayList<>();

    private VBox chatBox;
    int index = 0;

    @FXML
    private TextArea area;
    @FXML
    private ScrollPane container;

    public ChatBoxViewController() throws IOException {
    }

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

        if (!message.startsWith(chatHost.getName() + ":")) {
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
    void sendOnEnter(KeyEvent event) throws InterruptedException {
        if (event.getCode() == KeyCode.getKeyCode("Enter")) {
            if (!event.isShiftDown()) {
                //postMessage(area.getText());
                chatHost.sendMessage(area.getText());
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
        Timeline chatUpdater = new Timeline(
                new KeyFrame(Duration.seconds(0.1),
                        new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    for(String message : chatHost.receiveMessages()){
                                        postMessage(message);
                                    }
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }));
        chatUpdater.setCycleCount(Timeline.INDEFINITE);
        chatUpdater.play();
    }
}
