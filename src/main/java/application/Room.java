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

public class Room {

    private String chatFileName = "/ChatboxView.fxml";
    private String roomFileName = "/room-scene-view.fxml";

    private Scene roomScene;

    private FXMLLoader chatboxLoader;
    private FXMLLoader roomLoader;
    private RoomSceneViewController roomController;
    private Space space;
    private String name;
    private String uri;
    private int players = 0;
    private ArrayList<String> playerNames;

    public Room(Stage stage, GameApplication application, Space space) {
        this.space = space;
        playerNames = new ArrayList();
        try {
            uri = (String) space.query(new ActualField("clientUri"), new FormalField(String.class))[1];
            name = (String) space.get(new ActualField("name"), new FormalField(String.class))[1];

            updatePlayerNames(space);

            updateNumberOfPlayers(space);

            roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            populateChatBoxConstructor(uri, players, name);

            setupRoomLayout(stage, application);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);

    }

    private void updateNumberOfPlayers(Space space) throws InterruptedException {
        Object[] numberOfPlayers = space.getp(new ActualField("numberOfPlayer"), new FormalField(Integer.class));
        if (numberOfPlayers != null) {
            players = (int) numberOfPlayers[1];
        }
        players++;
        space.put("numberOfPlayer", players);
    }

    private void updatePlayerNames(Space space) throws InterruptedException {
        Object[] playerNameLists = space.getp(new ActualField("playerNameList"), new FormalField(ArrayList.class));
        if (playerNameLists != null) {
            playerNames = (ArrayList<String>) playerNameLists[1];
        }
        playerNames.add(name);
        space.put("playerNameList", playerNames);
    }


    private void setupRoomLayout(Stage stage, GameApplication application) throws IOException {
        BorderPane chatbox = chatboxLoader.load();
        BorderPane roomLayout = roomLoader.load();

        roomController = roomLoader.getController();

        Button lobbyButton = (Button) roomLayout.lookup("#lobbyButton");
        Button startGameButton = (Button) roomLayout.lookup("#startGameButton");
        lobbyButton.setOnAction(e -> stage.setScene(GameApplication.lobbyScene));
        startGameButton.setOnAction(e -> application.launchGame(stage));

        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);

        // After we've set up the scene we start the listener thread to update the ListView when newp players join
        new Thread(new RoomListener(space, roomController)).start();
    }

    private void populateChatBoxConstructor(String uri, int players, String name) {
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

class RoomListener implements Runnable {
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
                    roomController.updatePlayerList(newPlayerNames);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
