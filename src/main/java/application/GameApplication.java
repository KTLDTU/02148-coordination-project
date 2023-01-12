package application;

import datatypes.HashSetIntArray;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GameApplication {


    //public static final String HOST_IP = "10.209.96.93";
    private static String HOST_IP;
    public static final String PORT = ":9001";
    public static final String PROTOCOL = "tcp://";
    private static final int GAME_ID = 1535;
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;

    private Scene startScene;
    public String name = "defaultName";

    private boolean createLobby;
    SpaceRepository repository;
    SequentialSpace serverLobby;
    SequentialSpace serverRoom;
    SequentialSpace serverGameSpace;
    RemoteSpace clientLobby;
    RemoteSpace clientRoom;
    RemoteSpace clientGameSpace;

    public GameApplication(Stage stage, String HOST_IP, boolean createLobby) {
        this.HOST_IP = HOST_IP;
        this.createLobby = createLobby;
        try {
            makeStartScene(stage);


            repository = new SpaceRepository();
            serverLobby = new SequentialSpace();
            repository.add("lobby", serverLobby);
            String serverUri = PROTOCOL + HOST_IP + PORT + "/?keep";
            repository.addGate(serverUri);

            String clientUri = PROTOCOL + HOST_IP + PORT + "/lobby?keep";
            clientLobby = new RemoteSpace(clientUri);

            if (isHost())
                serverLobby.put("player id", 0);
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
        stage.centerOnScreen();
    }

    private void makeStartScene(Stage stage) {
        Label gameTitle = new Label("AZ");

        // Make buttons
        Button lobbyButton = new Button("Start lobby");
        lobbyButton.setPrefSize(150, 30);
        lobbyButton.setOnAction(e -> stage.setScene(ApplicationIntro.lobbyScene));

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

        startLayout.getChildren().addAll(gameTitle, lobbyButton, roomButton, gameButton, exitButton,startButton);

        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void launchRoom(Stage stage) {
        try {
            if (isHost()) {
                // temp name
                name = "bob";
                System.out.println("Host is creating a new room");
                serverRoom = new SequentialSpace();

                repository.add("room", serverRoom);
                String uri = PROTOCOL + HOST_IP + PORT + "/?keep";
                String clientUri = PROTOCOL + HOST_IP + PORT + "/room?keep";
                repository.addGate(uri);

                serverRoom.put("turn", 1);
                serverRoom.put("players", 1);
                serverRoom.put("readers", 0);

                serverRoom.put("clientUri", clientUri);
                serverRoom.put("name", name);
                new Room(stage, this, serverRoom);
            } else {
                // temp name
                name = "charlie";
                System.out.println("Client joining room");
                String uri = PROTOCOL + HOST_IP + PORT + "/room?keep";
                clientRoom = new RemoteSpace(uri);
                clientRoom.put("name", name);
                new Room(stage, this, clientRoom);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void launchGame(Stage stage) {
        try {
            ArrayList<Integer> playerIDs = new ArrayList<>(Arrays.asList(0, 1, 2, 3)); // assumed this is given from the room

            int playerID = (int) clientLobby.get(new ActualField("player id"), new FormalField(Integer.class))[1];
            clientLobby.put("player id", playerID + 1);
            System.out.println("Player id: " + playerID);
            Game game;

            if (isHost()) {
                System.out.println("Host is creating a new game...");
                serverGameSpace = new SequentialSpace();
                repository.add("gameSpace" + GAME_ID, serverGameSpace);

                game = new Game(stage, serverGameSpace, playerIDs, playerID);
                game.initializeGrid();
                game.spawnPlayers();
                game.gameSpace.put("connected squares", game.grid.connectedSquares);
            } else {
                System.out.println("Client is getting existing game...");
                String clientUri = PROTOCOL + HOST_IP + PORT + "/gameSpace" + GAME_ID + "?keep";
                clientGameSpace = new RemoteSpace(clientUri);

                game = new Game(stage, clientGameSpace, playerIDs, playerID);

                HashSetIntArray connectedSquares = (HashSetIntArray) clientGameSpace.query(new ActualField("connected squares"), new FormalField(HashSetIntArray.class))[1];
                game.setGrid(connectedSquares);
                game.spawnPlayers();
            }

            stage.setScene(game.gameScene);
            game.gameScene.getRoot().requestFocus();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isHost() {
        String ip;

        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return ip.equals(HOST_IP);
    }
}
