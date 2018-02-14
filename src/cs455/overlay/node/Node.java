package cs455.overlay.node;
import cs455.overlay.wireformats.Event;
import java.net.Socket;

public interface Node {
    public void onEvent(Event event, Socket socket);
}
