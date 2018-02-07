package cs455.overlay.transport;
import java.net.Socket;
import java.util.HashMap;
import cs455.overlay.node.Node;

public class TCPConnectionsCache {

    public TCPConnectionsCache(){
        connections = new HashMap();
    }
    HashMap<Integer,TCPConnection> connections;

    public void addConnection(Socket socket, Node node){
        TCPConnection conn  = new TCPConnection(socket, node);

    }
}
