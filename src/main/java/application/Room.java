package application;

import controllers.ChatBoxViewController;
import controllers.RoomSceneViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Room {

    private Scene roomScene;
    private ChatBoxViewController chatboxController;
    private RoomSceneViewController roomController;
    Space space;
    private int players = 0;

    public Room(Stage stage, GameApplication application, Space space) {
        this.space = space;
        // Chatbox resource file name
        String chatFileName = "/ChatboxView.fxml";
        String roomFileName = "/room-scene-view.fxml";
        try {
            FXMLLoader roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            FXMLLoader chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            String uri = (String) space.query(new ActualField("clientUri"), new FormalField(String.class))[1];
            String name = (String) space.get(new ActualField("name"), new FormalField(String.class))[1];

            // Try to update the number of players
            Object[] numberOfPlayers = space.getp(new ActualField("numberOfPlayer"), new FormalField(Integer.class));
            if (numberOfPlayers != null) {
                players = (int) numberOfPlayers[1];
            }
            players++;
            space.put("numberOfPlayer", players);

            populateChatBoxConstructor(chatboxLoader, uri, name);

            setupRoomLayout(stage, application, roomLoader, chatboxLoader);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);
    }

    private void setupRoomLayout(Stage stage, GameApplication application, FXMLLoader roomLoader, FXMLLoader chatboxLoader) throws IOException {
        BorderPane chatbox = chatboxLoader.load();
        BorderPane roomLayout = roomLoader.load();

        Button lobbyButton = (Button) roomLayout.lookup("#lobbyButton");
        Button startGameButton = (Button) roomLayout.lookup("#startGameButton");
        lobbyButton.setOnAction(e -> stage.setScene(application.lobbyScene));
        startGameButton.setOnAction(e -> application.launchGame(stage));


        chatbox.setMargin(chatbox, new Insets(0, 2, 0, 0));
        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);
    }

    private void populateChatBoxConstructor(FXMLLoader chatboxLoader, String uri, String name) {
        ArrayList arrayData = new ArrayList();
        arrayData.add(uri);
        arrayData.add(players);
        arrayData.add(name);

        ObservableList<String> data = FXCollections.observableArrayList(arrayData);
        chatboxLoader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                if (param == ChatBoxViewController.class) {
                    return new ChatBoxViewController(data);
                } else
                    try {
                        return param.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
            }
        });
    }
}
