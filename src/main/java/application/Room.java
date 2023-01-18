package application;

import controllers.ChatBoxViewController;
import controllers.RoomSceneViewController;
import datatypes.ArrayListInt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import listeners.RoomPlayerListener;
import listeners.StartGameListener;
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
    private String hostName;
    public static ArrayList<String> playerNames;

    private ArrayListInt playerIds;

    private String name;
    private String ip;
    private int numberOfPlayers;
    private Thread startGameListenerThread;
    private Thread roomPlayerListenerThread;

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

            roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            initializePlayerNames(roomSpace);
            initializePlayerIds(roomSpace);
            populateChatBoxConstructor(uri, playerId, name);

            setupRoomLayout(stage, application);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);

    }

    private void initializePlayerIds(Space space) {
        try {
            playerIds = (ArrayListInt) space.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
            playerIds.add(playerId);
            roomSpace.put("playerIdList", playerIds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializePlayerNames(Space space) {
        try {
            playerNames = (ArrayList<String>) space.get(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];
            playerNames.add(name);
            roomSpace.put("playerNameList", playerNames);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void setupRoomLayout(Stage stage, GameApplication application) throws IOException {
        BorderPane chatbox = chatboxLoader.load();
        BorderPane roomLayout = roomLoader.load();

        roomController = roomLoader.getController();

        // Start the listener thread to update the ListView when new players join
        roomPlayerListenerThread = new Thread(new RoomPlayerListener(roomSpace, roomController));
        roomPlayerListenerThread.setDaemon(true);
        roomPlayerListenerThread.start();

        if (!GameApplication.isRoomHost) {
            startGameListenerThread = new Thread(new StartGameListener(stage, roomSpace, application));
            startGameListenerThread.setDaemon(true);
            startGameListenerThread.start();
        }

        Button lobbyButton = (Button) roomLayout.lookup("#lobbyButton");
        Button startGameButton = (Button) roomLayout.lookup("#startGameButton");
        lobbyButton.setOnAction(e -> {
            try {
                System.out.println("Leaving room");
                if (!GameApplication.isRoomHost) {
                    startGameListenerThread.interrupt();
                }
                roomPlayerListenerThread.interrupt();
                ArrayList<String> playerNames = (ArrayList<String>) roomSpace.get(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];
                playerNames.remove(name);
                roomSpace.put("playerNameList", playerNames);

                ArrayListInt playerIds = (ArrayListInt) roomSpace.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
                playerIds.remove(Integer.valueOf(playerId));
                roomSpace.put("playerIdList", playerIds);

                String ip = (String) roomSpace.query(new ActualField("room ip"), new FormalField(String.class))[1];
                Object[] obj = Lobby.lobbySpace.get(new ActualField("room"), new ActualField(ip), new FormalField(String.class), new FormalField(Integer.class));
                Lobby.lobbySpace.put(obj[0], obj[1], obj[2], (Integer) obj[3] - 1);

                ChatBoxViewController chatboxController = chatboxLoader.getController();
                chatboxController.chatClient.closeClient();

                stage.setScene(GameApplication.lobbyScene);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        startGameButton.setOnAction(e -> {
            try {
                roomSpace.put("start game");
                application.launchGame(stage, roomSpace);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (!GameApplication.isRoomHost) startGameButton.setVisible(false);
        roomController.setRoomNameText(hostName);

        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);
    }

    private void populateChatBoxConstructor(String uri, int players, String name) {
        ArrayList arrayData = new ArrayList();
        arrayData.add(uri);
        arrayData.add(playerId);
        arrayData.add(name);

        ObservableList<String> data = FXCollections.observableArrayList(arrayData);
        chatboxLoader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                if (param == ChatBoxViewController.class) {
                    return new ChatBoxViewController(data, playerIds);
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

