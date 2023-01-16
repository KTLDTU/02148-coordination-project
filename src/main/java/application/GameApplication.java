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

    private static String HOST_IP;
    public static final String PORT = ":9001";
    public static final String PROTOCOL = "tcp://";
    private static final int GAME_ID = 1535;
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;

    public static Scene lobbyScene;
    private Scene startScene;
    public String name;

    public static boolean isHost;
    SpaceRepository repository;
    SequentialSpace serverLobby;
    SequentialSpace serverRoom;
    SequentialSpace serverGameSpace;
    RemoteSpace clientLobby;
    RemoteSpace clientRoom;
    RemoteSpace clientGameSpace;
    private int playerID;

    public GameApplication(Stage stage, String HOST_IP, boolean isHost, String name) {
        GameApplication.HOST_IP = HOST_IP;
        GameApplication.isHost = isHost;
        this.name = name;

        try {
            makeStartScene(stage);
            makeLobbyScene(stage);

            repository = new SpaceRepository();
            serverLobby = new SequentialSpace();
            serverRoom = new SequentialSpace();

            repository.add("lobby", serverLobby);
            repository.add("room", serverRoom);
            String serverUri = PROTOCOL + HOST_IP + PORT + "/?keep";
            repository.addGate(serverUri);

            String clientLobbyUri = PROTOCOL + HOST_IP + PORT + "/lobby?keep";
            String clientRoomUri = PROTOCOL + HOST_IP + PORT + "/room?keep";

            clientLobby = new RemoteSpace(clientLobbyUri);
            clientRoom = new RemoteSpace(clientRoomUri);

            if (isHost) {
                serverLobby.put("player id", 0);

                serverRoom.put("clientUri", clientRoomUri);
                serverRoom.put("turn", 1);
                serverRoom.put("players", 1);
                serverRoom.put("readers", 0);
            }
            playerID = (int) clientLobby.get(new ActualField("player id"), new FormalField(Integer.class))[1];
            clientLobby.put("player id", playerID + 1);
            System.out.println("Player id: " + playerID);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startGame(Stage stage) {
        showStartScene(stage);
        stage.show();
    }

    private void showStartScene(Stage stage) {
        stage.setScene(startScene);
    }

    private void makeStartScene(Stage stage) {
        Label gameTitle = new Label("AZ");

        // Make buttons
        Button lobbyButton = new Button("Start lobby");
        lobbyButton.setPrefSize(150, 30);
        lobbyButton.setOnAction(e -> stage.setScene(lobbyScene));

        Button roomButton = new Button("Start room");
        roomButton.setPrefSize(150, 30);
        roomButton.setOnAction(e -> launchRoom(stage));

        Button gameButton = new Button("Start game");
        gameButton.setPrefSize(150, 30);
        gameButton.setOnAction(e -> launchGame(stage));

        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        Button startButton = new Button("start main menu");
        startButton.setPrefSize(150, 30);
        startButton.setOnAction(e -> stage.setScene(ApplicationIntro.createOrJoinScene));

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, roomButton, gameButton, exitButton, startButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void makeLobbyScene(Stage stage) {
        try {
            FXMLLoader lobbyLoader = new FXMLLoader(LobbySceneController.class.getResource("/lobbyScene.fxml"));
            AnchorPane scene = lobbyLoader.load();
            LobbySceneController lobbyController = lobbyLoader.getController();
            lobbyScene = new Scene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchRoom(Stage stage) {
        try {
            clientRoom.put("name", name);
            clientRoom.put("player id", playerID);

            if (isHost) {
                System.out.println("Host is creating a new room");
                serverRoom.put("host name", name);
                new Room(stage, this, serverRoom);
            } else {
                System.out.println("Client joining room");
                new Room(stage, this, clientRoom);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void launchGame(Stage stage) {
        try {
            Game game;
            /*

            // Query the list of player names and id provided by room
            ArrayList<String> playerNameList = (ArrayList<String>) clientRoom.query(new ActualField("playerNameList"), new FormalField(ArrayList.class))[1];
            System.out.println("clientRoom playernamelist: " + playerNameList.toString());
            ArrayListInt playerIdList = (ArrayListInt) clientRoom.query(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
            System.out.println("clientRoom playerIdList: " + playerIdList.toString());

            // Collect the two lists to a map with id as keys and names as values
            Map<Integer, String> playersIdNameMap = IntStream.range(0, playerNameList.size()).boxed().collect(Collectors.toMap(i -> playerIdList.get(i), i -> playerNameList.get(i)));
             */

            // Temporary setup so the "start game" button doesnt break. Should be placed with commented code above
            Map<Integer, String> playersIdNameMap = new HashMap<>();
            playersIdNameMap.put(0, "Alice");
            playersIdNameMap.put(1, "Bob");
            playersIdNameMap.put(2, "Charlie");
            playersIdNameMap.put(3, "Frank");

            Object[] playerNameLists = clientRoom.queryp(new ActualField("playerNameList"), new FormalField(ArrayList.class));
            Object[] playerIdLists = clientRoom.queryp(new ActualField("playerIdList"), new FormalField(ArrayListInt.class));
            if (playerNameLists != null && playerIdLists != null) {
                ArrayList<String> playerNameList = (ArrayList<String>) playerNameLists[1];
                ArrayListInt playerIdList = (ArrayListInt) playerIdLists[1];
                playersIdNameMap = IntStream.range(0, playerNameList.size()).boxed().collect(Collectors.toMap(i -> playerIdList.get(i), i -> playerNameList.get(i)));
            }

            if (isHost) {
                System.out.println("Host is creating a new game...");
                serverGameSpace = new SequentialSpace();
                repository.add("gameSpace" + GAME_ID, serverGameSpace);

                game = new Game(stage, serverGameSpace, playersIdNameMap, playerID);
            } else {
                System.out.println("Client is getting existing game...");
                String clientUri = PROTOCOL + HOST_IP + PORT + "/gameSpace" + GAME_ID + "?keep";
                clientGameSpace = new RemoteSpace(clientUri);

                game = new Game(stage, clientGameSpace, playersIdNameMap, playerID);
            }

            game.newRound();
            stage.setScene(game.gameScene);
            game.gameScene.getRoot().requestFocus();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
