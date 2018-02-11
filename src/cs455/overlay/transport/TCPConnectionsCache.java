package cs455.overlay.transport;
import java.net.Socket;
import java.util.ArrayList;

import cs455.overlay.node.Node;
import cs455.overlay.routing.RoutingTable;

public class TCPConnectionsCache {
    private ArrayList<TCPConnection> connections;
    private Node node;

    public TCPConnectionsCache(Node node){
        connections = new ArrayList<>();
        this.node = node;
    }

    public void addConn(Socket newSock, int port){
        TCPConnection newConn = new TCPConnection(newSock, node);
        connections.add(newConn);
    }

    public void close(){
        for (TCPConnection conn : connections){
            conn.close();
        }
    }

    public TCPConnection getConnection(int id){
        return connections.get(id);
    }

    public void addConnection(Socket socket, Node node){
        TCPConnection conn = new TCPConnection(socket, node);
    }
}
