package cs455.overlay.transport;
import java.net.Socket;
import java.util.HashMap;
import cs455.overlay.node.Node;
import cs455.overlay.routing.RoutingTable;

public class TCPConnectionsCache {

    public TCPConnectionsCache(){
        routingTable = new RoutingTable();
    }

    public void addRegistry(String host, int port){
        try {
            regSocket = new Socket(host, port);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    private Socket regSocket;
    private RoutingTable routingTable;

    public void connectTo(Socket socket, Node node){
        TCPConnection conn  = new TCPConnection(socket, node);
    }

    public void addConnection(Socket socket, Node node){
        TCPConnection conn = new TCPConnection(socket, node);
    }
}
