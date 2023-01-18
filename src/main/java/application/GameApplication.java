package application;

import controllers.LobbySceneController;
import datatypes.ArrayListInt;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameApplication {

    private static String LOBBY_HOST_IP;
    public static final String PORT = ":9001";
    public static final String PROTOCOL = "tcp://";
    private static final int GAME_ID = 1535;
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;

    public static Scene lobbyScene;
    public static Scene startScene;
    public String name;

    public static boolean isServerHost;
    public static boolean isRoomHost;
    public SpaceRepository repository;
    SequentialSpace serverLobby;
    SequentialSpace serverRoom;
    SequentialSpace serverGameSpace;
    RemoteSpace clientLobby;
    RemoteSpace clientRoom;
    RemoteSpace clientGameSpace;
    public int playerID;

    public GameApplication(Stage stage, String lobbyHostIP, boolean isServerHost, String name) {
        LOBBY_HOST_IP = lobbyHostIP;
        this.isServerHost = isServerHost;
        this.name = name;

        try {
            repository = new SpaceRepository();
            serverLobby = new SequentialSpace();

            repository.add("lobby", serverLobby);
            String serverUri = PROTOCOL + LOBBY_HOST_IP + PORT + "/?keep";
            repository.addGate(serverUri);

            String clientLobbyUri = PROTOCOL + LOBBY_HOST_IP + PORT + "/lobby?keep";

            clientLobby = new RemoteSpace(clientLobbyUri);

            if (isServerHost) {
                serverLobby.put("player id", 0);
            }
            playerID = (int) clientLobby.get(new ActualField("player id"), new FormalField(Integer.class))[1];
            clientLobby.put("player id", playerID + 1);
            System.out.println("Player id: " + playerID);

            launchLobby(stage);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startGame(Stage stage) {
        showLobbyScene(stage);
        stage.show();
    }

    private void showLobbyScene(Stage stage) {
        stage.setScene(lobbyScene);
    }

    private void launchLobby(Stage stage) {
        if (isServerHost) {
            new Lobby(stage, this, serverLobby);
        } else {
            new Lobby(stage, this, clientLobby);
        }
    }

    public void launchGame(Stage stage, Space roomSpace) {
        try {
            Game game;

            // Query the list of player names and id provided by room
            ArrayList<String> playerNameList = (ArrayList<String>) roomSpace.query(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];
            System.out.println("clientRoom playernamelist: " + playerNameList.toString());
            ArrayListInt playerIdList = (ArrayListInt) roomSpace.query(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
            System.out.println("clientRoom playerIdList: " + playerIdList.toString());
            Object[] roomObjs = roomSpace.query(new ActualField("room ip"), new FormalField(String.class));
            String ip = (String) roomObjs[1];
            // Collect the two lists to a map with id as keys and names as values
            Map<Integer, String> playersIdNameMap = IntStream.range(0, playerNameList.size()).boxed().collect(Collectors.toMap(i -> playerIdList.get(i), i -> playerNameList.get(i)));

            if (isRoomHost) {
                clientLobby.get(new ActualField("room"), new ActualField(ip), new FormalField(String.class), new FormalField(Integer.class));
                System.out.println("Host is creating a new game...");
                serverGameSpace = new SequentialSpace();
                repository.add("gameSpace" + GAME_ID, serverGameSpace);

                game = new Game(stage, serverGameSpace, playersIdNameMap, playerID);
            } else {
                System.out.println("Client is getting existing game...");
                String clientUri = PROTOCOL + ip + PORT + "/gameSpace" + GAME_ID + "?keep";
                System.out.println("game client uri: " + clientUri);
                clientGameSpace = new RemoteSpace(clientUri);

                game = new Game(stage, clientGameSpace, playersIdNameMap, playerID);
            }

            stage.setScene(game.gameScene);
            game.gameScene.getRoot().requestFocus();
            game.newRound();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
