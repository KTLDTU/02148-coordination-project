package application;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class LobbyConnector {
    private String name;
    private Space space;
    public LobbyConnector(String uri, String name) throws URISyntaxException, IOException, InterruptedException {
        this.name = name;
        if(uri == null) uri = "tcp://127.0.0.1:9001/lobby?keep";
        URI lobbyUri = new URI(uri);
        String gateUri = "tcp://" + lobbyUri.getHost() + ":" + lobbyUri.getPort() + "lobby?keep";
        space = new RemoteSpace(uri);
        Object[] players = space.get(new ActualField("players"), new FormalField(Integer.class));
        space.put("players",(int)players[1]+1);
        space.put("player",name);
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
