package application;

import controllers.LobbySceneController;
import datatypes.ArrayListInt;
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
    public static Space lobbySpace;
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
            // Start listener to update the roomlist
            Thread roomListListener = new Thread(new RoomListListener(lobbySpace, roomList));
            roomListListener.setDaemon(true);
            roomListListener.start();
            roomList.setOnMouseClicked(e -> {
                if (e.getClickCount() >= 2 && roomList.getSelectionModel().getSelectedItem() != null) {
                    Room selectedRoom = roomList.getSelectionModel().getSelectedItem();
                    // Disallow joining a room thats full
                    if (selectedRoom.getNumberOfPlayers() == 4) return;
                    String ip = selectedRoom.getIp();
                    joinRoom(ip);
                    launchRoom(roomClient);
                }
            });
            GameApplication.lobbyScene = new Scene(root, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void createRoom(String ip) {
        ip = "25.20.181.255";
        try {
            GameApplication.isRoomHost = true;
            roomHost = new SequentialSpace();
            String uri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/?keep";
            application.repository.add("room", roomHost);
            application.repository.addGate(uri);
            String clientRoomUri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/room?keep";
            System.out.println("Client room URI: " + clientRoomUri);
            // Create room thats visible from the lobby
            lobbySpace.put("room", ip, name, 1);
            roomHost.put("playerNameList", new ArrayList<String>());
            roomHost.put("playerIdList", new ArrayListInt());
            roomHost.put("clientUri", clientRoomUri);
            roomHost.put("hostID", playerID);
            roomHost.put("room ip", ip);
            roomHost.put("turn", 1);
            roomHost.put("players", 1);
            roomHost.put("readers", 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void joinRoom(String ip) {
        try {
            GameApplication.isRoomHost = false;
            // Increment number of players by 1
            Object[] obj = lobbySpace.get(new ActualField("room"), new ActualField(ip), new FormalField(String.class), new FormalField(Integer.class));
            System.out.println("got lobbyspace room");
            String uri = GameApplication.PROTOCOL + ip + GameApplication.PORT + "/room?keep";
            System.out.println("join room uri: " + uri);
            roomClient = new RemoteSpace(uri);
            System.out.println("roomClient size: " + roomClient.size());
            Object[] obj2 = roomClient.queryp(new ActualField("hostID"), new FormalField(Integer.class));
            if (obj2 != null) {
                int hostID = (int) obj2[1];
                if (playerID == hostID) {
                    GameApplication.isRoomHost = true;
                }
            }
            System.out.println("put room back to lobby space");
            lobbySpace.put(obj[0], obj[1], obj[2], (Integer) obj[3] + 1);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchRoom(Space roomSpace) {
        try {
            System.out.println("roomSpace size: " + roomSpace.size());
            if (roomClient != null) {

                System.out.println("roomClient size: " + roomClient.size());
            }
            roomSpace.put("name", name);
            roomSpace.put("player id", playerID);

            if (GameApplication.isRoomHost) {
                System.out.println("Host is creating a new room");
                roomSpace.put("host name", name);
            } else { // TODO: remove else statement
                System.out.println("Client joining room");
            }
            new Room(stage, application, roomSpace);
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
                // Query all the rooms and create a new list of rooms
                List<Object[]> newRoomObjects = lobbySpace.queryAll(new ActualField("room"), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
                ArrayList<Room> newRoomList = new ArrayList<>();
                for (Object[] objects : newRoomObjects) {
                    newRoomList.add(new Room((String) objects[1], (String) objects[2], (Integer) objects[3]));
                }
                // Check if the old roomList and newRoomList differ, if so update the ListView
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
