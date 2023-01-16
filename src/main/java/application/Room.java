package application;

import controllers.ChatBoxViewController;
import controllers.RoomSceneViewController;
import datatypes.ArrayListInt;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
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
import java.util.Arrays;

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
    private int playerId;
    private String hostName;
    public static ArrayList<String> playerNames;
    private ArrayListInt playerIds;

    public Room(Stage stage, GameApplication application, Space space) {
        this.space = space;
        this.playerNames = new ArrayList<>();
        this.playerIds = new ArrayListInt();
        try {
            uri = (String) space.query(new ActualField("clientUri"), new FormalField(String.class))[1];
            hostName = (String) space.query(new ActualField("host name"), new FormalField(String.class))[1];
            name = (String) space.get(new ActualField("name"), new FormalField(String.class))[1];
            playerId = (int) space.get(new ActualField("player id"), new FormalField(Integer.class))[1];

            roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            initializePlayerNames(space);
            initializePlayerIds(space);
            populateChatBoxConstructor(uri, playerNames.size(), name);

            setupRoomLayout(stage, application);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);

    }

    private void initializePlayerIds(Space space) throws InterruptedException {
        Object[] listOfPlayerIds = space.getp(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
        if (listOfPlayerIds != null) {
            playerIds = (ArrayListInt) listOfPlayerIds[1];
        }
        playerIds.add(playerId);
        space.put("playerIdList", playerIds);
    }

    private void initializePlayerNames(Space space) throws InterruptedException {
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
        lobbyButton.setOnAction(e -> {
            try {
                ArrayList<String> playerNames = (ArrayList<String>) space.get(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];
                playerNames.remove(name);
                space.put("playerNameList", playerNames);

                ArrayListInt playerIds = (ArrayListInt) space.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
                playerIds.remove(Integer.valueOf(playerId));
                space.put("playerIdList", playerIds);

                ChatBoxViewController chatboxController = chatboxLoader.getController();
                chatboxController.chatClient.closeClient();

                // TODO: switch to lobby scene
                stage.setScene(GameApplication.startScene);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        startGameButton.setOnAction(e -> {
            try {
                application.launchGame(stage);
                for (int i = 0; i < playerNames.size() - 1; i++) {
                    space.put("start game");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        if (!GameApplication.isHost) startGameButton.setVisible(false);
        roomController.setRoomNameText(hostName);

        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);

        // After we've set up the scene we start the listener thread to update the ListView when newp players join
        new Thread(new RoomPlayerListener(space, roomController)).start();

        if (!GameApplication.isHost)
            new Thread(new StartGameListener(stage, space, application)).start();
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

class RoomPlayerListener implements Runnable {
    Space space;
    RoomSceneViewController roomController;
    ArrayList<String> playerNames = new ArrayList<>();
    ArrayListInt playerIds = new ArrayListInt();

    public RoomPlayerListener(Space space, RoomSceneViewController roomController) {
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
                    Platform.runLater(() -> roomController.updatePlayerList(newPlayerNames));
                    Room.playerNames = newPlayerNames;
                }
                ArrayListInt newPlayerIds = (ArrayListInt) space.query(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];

                if (!playerIds.equals(newPlayerIds)) {
                    space.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
                    playerIds = newPlayerIds;
                    space.put("playerIdList", playerIds);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class StartGameListener implements Runnable {

    private Stage stage;
    private Space space;
    private GameApplication application;

    public StartGameListener(Stage stage, Space space, GameApplication application) {
        this.stage = stage;
        this.space = space;
        this.application = application;
    }

    @Override
    public void run() {
        try {
            space.get(new ActualField("start game"));
            Platform.runLater(() -> application.launchGame(stage));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}