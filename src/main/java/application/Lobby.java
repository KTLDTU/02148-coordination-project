package application;

import org.jspace.RandomSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.net.URI;
import java.net.URISyntaxException;

public class Lobby {
    private SpaceRepository spaceRepository = new SpaceRepository();
    private Space space = new SequentialSpace();
    private URI uri;

    public Lobby(String uri) throws URISyntaxException, InterruptedException {
        if (uri == null) {
            uri = "tcp://127.0.0.1:9001/?keep";
        }
        this.uri = new URI(uri);
        String gateUri = "tcp://" + this.uri.getHost() + ":" + this.uri.getPort() + "?keep";
        spaceRepository.addGate(gateUri);
        spaceRepository.add("lobby",space);
        space.put("players",0);
    }

}
