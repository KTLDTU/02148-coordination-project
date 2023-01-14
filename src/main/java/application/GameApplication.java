package application;

import controllers.LobbySceneController;
import controllers.PlayerNameInputController;
import datatypes.HashSetIntArray;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GameApplication {

    public static final String HOST_IP = "192.168.1.7";
    public static final String PORT = ":9001";
    public static final String PROTOCOL = "tcp://";
    private static final int GAME_ID = 1535;
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;

    private Scene nameInputScene;
    private Scene startScene;
    public Scene lobbyScene;
    public String name = "defaultName";

    SpaceRepository repository;
    SequentialSpace serverLobby;
    SequentialSpace serverRoom;
    SequentialSpace serverGameSpace;
    RemoteSpace clientLobby;
    RemoteSpace clientRoom;
    RemoteSpace clientGameSpace;

    public GameApplication(Stage stage) {
        try {
            Lobby lobby = new Lobby("192.168.1.7");
            makeNameInputScene(stage);
            makeStartScene(stage);
            makeLobbyScene(stage);

            repository = new SpaceRepository();
            serverLobby = new SequentialSpace();
            repository.add("lobby", serverLobby);
            String serverUri = PROTOCOL + HOST_IP + PORT + "/?keep";
            repository.addGate(serverUri);

            String clientUri = PROTOCOL + HOST_IP + PORT + "/lobby?keep";
            clientLobby = new RemoteSpace(clientUri);

            if (isHost())
                serverLobby.put("player id", 0);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void startGame(Stage stage) {
        showStartScene(stage);
        stage.show();
    }

    private void showStartScene(Stage stage) {
        stage.setScene(nameInputScene);
        stage.centerOnScreen();
    }

    private void makeStartScene(Stage stage) {
        Label gameTitle = new Label("AZ");

        // Make buttons
        Button lobbyButton = new Button("Start lobby");
        lobbyButton.setPrefSize(150, 30);
        lobbyButton.setOnAction(e -> makeLobbyScene(stage));
        Button roomButton = new Button("Start room");
        roomButton.setPrefSize(150, 30);
        roomButton.setOnAction(e -> launchRoom(stage));
        Button gameButton = new Button("Start game");
        gameButton.setPrefSize(150, 30);
        gameButton.setOnAction(e -> launchGame(stage));
        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, roomButton, gameButton, exitButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void launchRoom(Stage stage) {
        try {
            if (isHost()) {
                System.out.println("Host is creating a new room");
                serverRoom = new SequentialSpace();

                repository.add("room", serverRoom);
                String uri = PROTOCOL + HOST_IP + PORT + "/?keep";
                String clientUri = PROTOCOL + HOST_IP + PORT + "/room?keep";
                repository.addGate(uri);

                serverRoom.put("turn", 1);
                serverRoom.put("players", 1);
                serverRoom.put("readers", 0);

                serverRoom.put("clientIp", HOST_IP);
                serverRoom.put("name", name);
                new Room(stage, this, serverRoom);
            } else {
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

    private void makeLobbyScene(Stage stage) {
        try {
            new LobbyConnector(stage, this, "192.168.1.7", name);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeNameInputScene(Stage stage) {
        try {
            FXMLLoader playerInputLoader = new FXMLLoader(PlayerNameInputController.class.getResource("/player-name-input.fxml"));
            VBox scene = playerInputLoader.load();
            PlayerNameInputController playerNameInputController = playerInputLoader.getController();
            playerNameInputController.continueButton.setOnAction(e -> {
                String nameInput = playerNameInputController.inputNameField.getText().trim();
                if (!nameInput.isEmpty()) {
                    name = nameInput;
                }
                stage.setScene(startScene);
            });
            nameInputScene = new Scene(scene, WINDOW_WIDTH, WINDOW_HEIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
