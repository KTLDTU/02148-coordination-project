package application;

import controllers.LobbySceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.jspace.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

public class LobbyConnector {
    private String lobbyFileName = "/lobbyScene.fxml";
    private FXMLLoader lobbyLoader;
    private Stage stage;
    private String name;
    private Space space;
    public LobbyConnector(Stage stage, GameApplication application, String ip, String name) throws URISyntaxException, IOException, InterruptedException {
        this.stage = stage;
        this.name = name;
        if(ip == null) ip = "127.0.0.1";
        URI lobbyUri = new URI("tcp://" + ip + ":9002/lobby?keep");
        space = new RemoteSpace(lobbyUri);
        Object[] players = space.get(new ActualField("players"), new FormalField(Integer.class));
        space.put("players",(int)players[1]+1);
        space.put("player",name);

        lobbyLoader = new FXMLLoader(LobbySceneController.class.getResource(lobbyFileName));
        AnchorPane lobby = lobbyLoader.load();
        Button createRoomButton = (Button)lobby.lookup("#createroom");

        Button refresh = (Button)lobby.lookup("#refresh");
        ListView rooms = (ListView)lobby.lookup("#rooms");
        String finalIp = ip;
        createRoomButton.setOnAction(e -> {
            try {
                createRoom("uri");
                application.launchRoom(stage);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        refresh.setOnAction(e -> {
            rooms.getItems().remove(0,rooms.getItems().size());
            try {
                for(Object[] room : getRooms()){
                    rooms.getItems().add(room[2]);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        rooms.setOnMouseClicked(e -> {
            if(e.getClickCount() >= 2 && rooms.getSelectionModel().getSelectedItem() != null){
                Label playerAmount = (Label)lobby.lookup("#playerAmount");
                playerAmount.setText("Hello, why does this not work?");
                try {
                    playerAmount.setText("Joining room: " + joinRoom("" + rooms.getSelectionModel().getSelectedItem()));
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                application.launchRoom(stage);
            }
        });
        stage.setScene(new Scene(lobby, GameApplication.WINDOW_WIDTH, GameApplication.WINDOW_HEIGHT));
    }
    public int getPlayerAmount() throws InterruptedException {
        return (int)space.queryp(new ActualField("players"), new FormalField(Integer.class))[1];
    }
    public void createRoom(String uri) throws InterruptedException {
        Object[] players = space.get(new ActualField("players"), new FormalField(Integer.class));
        space.put("players",(int)players[1]-1);
        space.getp(new ActualField("player"),new ActualField(name));
        space.put("room", uri, name);
    }
    public String joinRoom(String name) throws InterruptedException {
        Object[] players = space.get(new ActualField("players"), new FormalField(Integer.class));
        space.put("players",(int)players[1]-1);
        space.getp(new ActualField("player"),new ActualField(name));
        return (String)space.queryp(new ActualField("room"), new FormalField(String.class),new ActualField(name))[1];
    }
    public List<Object[]> getRooms() throws InterruptedException {
        return space.queryAll(new ActualField("room"), new FormalField(String.class),new FormalField(String.class));
    }
}
