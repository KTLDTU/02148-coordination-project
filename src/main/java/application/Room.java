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
import java.util.Objects;

public class Room {

    private String chatFileName = "/ChatboxView.fxml";
    private String roomFileName = "/room-scene-view.fxml";

    private Scene roomScene;

    private FXMLLoader chatboxLoader;
    private FXMLLoader roomLoader;
    private RoomSceneViewController roomController;
    private Space roomSpace;
    private String uri;
    private int playerId;
    public static ArrayList<String> playerNames;

    private ArrayListInt playerIds;
    private String hostName;

    private String name;
    private String ip;
    private int numberOfPlayers;

    // Constructor used for RoomCell in ListView
    public Room(String ip, String name, int numberOfPlayers) {
        this.ip = ip;
        this.name = name;
        this.numberOfPlayers = numberOfPlayers;
    }

    public Room(Stage stage, GameApplication application, Space roomSpace) {
        this.roomSpace = roomSpace;
        playerNames = new ArrayList();
        playerIds = new ArrayListInt();
        try {
            uri = (String) roomSpace.query(new ActualField("clientUri"), new FormalField(String.class))[1];
            hostName = (String) roomSpace.query(new ActualField("host name"), new FormalField(String.class))[1];
            name = (String) roomSpace.get(new ActualField("name"), new FormalField(String.class))[1];
            playerId = (int) roomSpace.get(new ActualField("player id"), new FormalField(Integer.class))[1];

            updatePlayerNames(roomSpace);

            updatePlayerIds(roomSpace);
            roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            populateChatBoxConstructor(uri, playerNames.size(), name);

            setupRoomLayout(stage, application);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);

    }

    private void updatePlayerIds(Space roomSpace) throws InterruptedException {
        Object[] listOfPlayerIds = roomSpace.getp(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
        if (listOfPlayerIds != null) {
            playerIds = (ArrayListInt) listOfPlayerIds[1];
        }
        playerIds.add(playerId);
        roomSpace.put("playerIdList", playerIds);
    }

    private void updatePlayerNames(Space roomSpace) throws InterruptedException {
        Object[] playerNameLists = roomSpace.getp(new ActualField("playerNameList"), new FormalField(ArrayList.class));
        if (playerNameLists != null) {
            playerNames = (ArrayList<String>) playerNameLists[1];
        }
        playerNames.add(name);
        roomSpace.put("playerNameList", playerNames);
    }


    private void setupRoomLayout(Stage stage, GameApplication application) throws IOException {
        BorderPane chatbox = chatboxLoader.load();
        BorderPane roomLayout = roomLoader.load();

        roomController = roomLoader.getController();

        Button lobbyButton = (Button) roomLayout.lookup("#lobbyButton");
        Button startGameButton = (Button) roomLayout.lookup("#startGameButton");
        lobbyButton.setOnAction(e -> stage.setScene(GameApplication.lobbyScene));
        startGameButton.setOnAction(e -> {
            try {
                for (int i = 0; i < playerNames.size() - 1; i++) {
                    roomSpace.put("start game");
                }
                application.launchGame(stage, roomSpace);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (!GameApplication.isRoomHost) startGameButton.setVisible(false);
        roomController.setRoomNameText(hostName);

        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);

        // After we've set up the scene we start the listener thread to update the ListView when newp players join
        new Thread(new RoomPlayerListener(roomSpace, roomController)).start();

        if (!GameApplication.isRoomHost)
            new Thread(new StartGameListener(stage, roomSpace, application)).start();
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

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    // Two rooms are equal if they have the same id, ip, name and number of players
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(ip, room.ip) &&
                Objects.equals(name, room.name) &&
                numberOfPlayers == room.numberOfPlayers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, name, numberOfPlayers);
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

}

class RoomPlayerListener implements Runnable {
    Space roomSpace;
    RoomSceneViewController roomController;
    ArrayList<String> playerNames = new ArrayList<>();

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
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

class StartGameListener implements Runnable {

    private Stage stage;
    private Space roomSpace;
    private GameApplication application;

    public StartGameListener(Stage stage, Space roomSpace, GameApplication application) {
        this.stage = stage;
        this.roomSpace = roomSpace;
        this.application = application;
    }

    @Override
    public void run() {
        try {
            roomSpace.get(new ActualField("start game"));
            Platform.runLater(() -> application.launchGame(stage, roomSpace));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}