package application;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import controllers.ChatBoxViewController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.net.*;

public class GameApplication {

    private Scene roomScene;
    private Scene startScene;
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 540;
    public static final String HOST_IP = "192.168.72.216";

    SpaceRepository repository;
    SequentialSpace serverLobby;
    RemoteSpace clientLobby;

    public GameApplication(Stage stage) {
        makeStartScene(stage);
        makeRoomScene(stage);
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
        lobbyButton.setOnAction(e -> stage.setScene(roomScene));
        Button gameButton = new Button("Start game");
        gameButton.setPrefSize(150, 30);
        gameButton.setOnAction(e -> launchGame(stage));
        Button exitButton = new Button("Exit");
        exitButton.setPrefSize(150, 30);
        exitButton.setOnAction(e -> stage.close());

        // Make layout and insert buttons
        VBox startLayout = new VBox(20);
        startLayout.getChildren().addAll(gameTitle, lobbyButton, gameButton, exitButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void launchGame(Stage stage) {
        try {
            Game game;

            if (isHost()) {
                System.out.println("Host is creating a new game...");
                game = new Game(stage);

                repository = new SpaceRepository();
                serverLobby = new SequentialSpace();
                repository.add("lobby", serverLobby);
                String uri = "tcp://" + HOST_IP + ":9001/?keep";
                repository.addGate(uri);

                serverLobby.put("game", game); // TODO: game needs to be serialized
            }
            else {
                System.out.println("Client is getting existing game...");

                String uri = "tcp://" + HOST_IP + ":9001/lobby?keep";
                clientLobby = new RemoteSpace(uri);

                game = (Game) clientLobby.query(new ActualField("game"), new FormalField(Game.class))[1];
            }

            stage.setScene(game.gameScene);
            Player player = new Player(game);
            game.addPlayer(player);
            game.gameScene.getRoot().requestFocus();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isHost() {
        String ip;

        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return ip.equals(HOST_IP);
    }

    private void makeRoomScene(Stage stage) {
        BorderPane roomLayout = new BorderPane();

        // Chatbox resource file name
        String fileName = "/ChatboxView.fxml";
        try {
            FXMLLoader chatboxLoader = new FXMLLoader(ChatBoxViewController.class.getResource(fileName));
            BorderPane chatbox = chatboxLoader.load();
            chatbox.setMargin(chatbox, new Insets(0,2, 0,0));
            roomLayout.setRight(chatbox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        roomScene = new Scene(roomLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}
