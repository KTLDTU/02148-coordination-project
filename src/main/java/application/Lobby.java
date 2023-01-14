package application;

import org.jspace.*;

import java.net.URI;
import java.net.URISyntaxException;

public class Lobby {
    private SpaceRepository spaceRepository = new SpaceRepository();
    private Space space = new SequentialSpace();
    private URI ip;

    public Lobby(String ip) throws URISyntaxException, InterruptedException {
        if (ip == null) {
            ip = "127.0.0.1";
        }
        this.ip = new URI("tcp://" + ip + ":9002/?keep");
        String gateUri = "tcp://" + this.ip.getHost() + ":" + this.ip.getPort() + "?keep";
        spaceRepository.addGate(gateUri);
        spaceRepository.add("lobby",space);
        space.put("players",0);
    }
    public int getPlayerAmount() throws InterruptedException {
        return (int)space.queryp(new ActualField("players"), new FormalField(Integer.class))[1];
    }
}
