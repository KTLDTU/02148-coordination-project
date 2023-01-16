package application;

import controllers.ChatBoxViewController;
import controllers.RoomSceneViewController;
import datatypes.ArrayListInt;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jspace.*;

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
    private String ip;
    private boolean isHost;
    private int playerId;
    public static ArrayList<String> playerNames;
    private ArrayListInt playerIds;
    private String hostName;
    private SpaceRepository repository = new SpaceRepository();
    private Space gameSpace = new SequentialSpace();

    public Room(Stage stage, GameApplication application, String ip, boolean isHost,String name) {
        this.name = name;
        this.ip = ip;
        this.isHost = isHost;
        playerNames = new ArrayList();
        playerIds = new ArrayListInt();
        try {
            if(isHost){
                space = new SequentialSpace();
                repository.add("room", space);
                repository.addGate("tcp://" + ip + ":9001/?keep");
                space.put("clientUri", ip);
                space.put("turn", 1);
                space.put("players", 1);
                space.put("readers", 0);
                space.put("room", ip, this.name);
            } else {
                this.space = new RemoteSpace("tcp://" + ip + ":9001/room?keep");
            }
            updatePlayerNames(space);

            updatePlayerIds(space);

            roomLoader = new FXMLLoader(RoomSceneViewController.class.getResource(roomFileName));
            chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(chatFileName));

            populateChatBoxConstructor(ip, playerNames.size(), name);

            setupRoomLayout(stage, application);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        stage.setScene(roomScene);

    }

    private void updatePlayerIds(Space space) throws InterruptedException {
        Object[] listOfPlayerIds = space.getp(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
        if (listOfPlayerIds != null) {
            playerIds = (ArrayListInt) listOfPlayerIds[1];
        }
        playerIds.add(playerId);
        space.put("playerIdList", playerIds);
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
        lobbyButton.setOnAction(e -> application.makeLobbyScene(stage));
        startGameButton.setOnAction(e -> {
            repository.add("gameSpace" + 1535, gameSpace);
            try {
                application.launchGame(stage, ip, isHost);
                for (int i = 0; i < playerNames.size() - 1; i++) {
                    space.put("start game");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        if (!isHost) startGameButton.setVisible(false);
        roomController.setRoomNameText(hostName);

        roomLayout.setRight(chatbox);
        roomScene = new Scene(roomLayout, application.WINDOW_WIDTH, application.WINDOW_HEIGHT);

        // After we've set up the scene we start the listener thread to update the ListView when newp players join
        new Thread(new RoomPlayerListener(space, roomController)).start();

        if (!isHost)
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