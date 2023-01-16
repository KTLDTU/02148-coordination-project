package application;

import controllers.LobbySceneController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private Stage stage;
    private GameApplication application;
    private Space lobbySpace;
    private String lobbyFileName = "/lobbyScene.fxml";

    private SequentialSpace roomHost;
    private RemoteSpace roomClient;
    private String name;
    private int playerID;
    public ListView<Room> roomList;

    public Lobby(Stage stage, GameApplication application, Space lobbySpace) {
        this.stage = stage;
        this.application = application;
        this.lobbySpace = lobbySpace;
        name = application.name;
        playerID = application.playerID;
        setupLobbyLayout();
    }

    private void setupLobbyLayout() {
        try {
            FXMLLoader lobbyLoader = new FXMLLoader(LobbySceneController.class.getResource(lobbyFileName));
            AnchorPane root = lobbyLoader.load();
            LobbySceneController lobbyController = lobbyLoader.getController();
            Button createRoomButton = lobbyController.createRoomButton;
            roomList = lobbyController.roomList;

            createRoomButton.setOnAction(e -> {
                createRoom(getIp());
                launchRoom(roomHost);
            });
            Thread roomListListener = new Thread(new RoomListListener(lobbySpace, roomList));
            roomListListener.setDaemon(true);
            roomListListener.start();
            roomList.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2 && roomList.getSelectionModel().getSelectedItem() != null) {
                    Room selectedRoom = roomList.getSelectionModel().getSelectedItem();
                    if (selectedRoom.getNumberOfPlayers() == 4) return;
                    int roomId = selectedRoom.getRoomId();
                    joinRoom(getIp(), roomId);
                    launchRoom(roomClient);
                }
            });
            GameApplication.lobbyScene = new Scene(root, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void createRoom(String ip) {
        try {
            GameApplication.isRoomHost = true;
            roomHost = new SequentialSpace();
            String uri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/?keep";
            int roomId = getUpdatedRoomId();
            System.out.println("Creating room " + roomId);
            application.repository.add("room" + roomId, roomHost);
            application.repository.addGate(uri);
            String room = "room" + roomId;
            String clientRoomUri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/" + room + "?keep";

            lobbySpace.put("room", ip, roomId, name, 1);
            roomHost.put("clientUri", clientRoomUri);
            roomHost.put("turn", 1);
            roomHost.put("players", 1);
            roomHost.put("readers", 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void joinRoom(String ip, int roomId) {
        try {
            GameApplication.isRoomHost = false;
            String room = "room" + roomId;
            String uri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/" + room + "?keep";
            // Increment number of players by 1
            Object[] obj = lobbySpace.get(new ActualField("room"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(String.class), new FormalField(Integer.class));
            lobbySpace.put(obj[0], obj[1], obj[2], obj[3], (Integer) obj[4] + 1);
            roomClient = new RemoteSpace(uri);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchRoom(Space roomSpace) {
        try {
            roomSpace.put("name", name);
            roomSpace.put("player id", playerID);
            int roomId = getRoomId();
            roomSpace.put("room id", roomId);

            if (GameApplication.isRoomHost) {
                System.out.println("Host is creating a new room");
                roomSpace.put("host name", name);
            } else {
                System.out.println("Client joining room");
            }
            new Room(stage, application, roomSpace, roomId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int getUpdatedRoomId() {
        try {
            Object[] obj = lobbySpace.getp(new ActualField("room id"), new FormalField(Integer.class));
            int roomId = obj == null ? 0 : (int) obj[1] + 1;
            lobbySpace.put("room id", roomId);
            return roomId;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int getRoomId() {
        try {
            Object[] obj = lobbySpace.queryp(new ActualField("room id"), new FormalField(Integer.class));
            int roomId = obj == null ? 0 : (int) obj[1];
            return roomId;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getIp() {
        String ip;

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

}

class RoomListListener implements Runnable {

    private Space lobbySpace;

    private ArrayList<Room> roomList = new ArrayList<>();
    private ListView<Room> roomListView;

    public RoomListListener(Space lobbySpace, ListView<Room> roomListView) {
        this.lobbySpace = lobbySpace;
        this.roomListView = roomListView;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Query all the rooms and create a new list
                List<Object[]> newRoomObjects = lobbySpace.queryAll(new ActualField("room"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(String.class), new FormalField(Integer.class));
                ArrayList<Room> newRoomList = new ArrayList<>();
                for (Object[] objects : newRoomObjects) {
                    newRoomList.add(new Room((String) objects[1], (Integer) objects[2], (String) objects[3], (Integer) objects[4]));
                }
                // Check if the old roomList and newRoomList are equal
                if (!(roomList.containsAll(newRoomList) && newRoomList.containsAll(roomList))) {
                    roomList = newRoomList;
                    Platform.runLater(() -> roomListView.setItems(FXCollections.observableArrayList(roomList)));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
