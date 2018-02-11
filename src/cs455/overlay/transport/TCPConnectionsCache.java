package cs455.overlay.transport;
import java.net.InetAddress;
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

    public synchronized void close(){
        for (TCPConnection conn : connections){
            conn.close();
        }
    }


    public synchronized TCPConnection getConnection(int id){
        return connections.get(id);
    }

    public synchronized Socket getSocket(InetAddress address, int port){
        Socket rSocket;

        for(TCPConnection conn : connections){
            rSocket = conn.getSocket();
            if(rSocket.getInetAddress().equals(address) && rSocket.getPort() == port){
                return rSocket;
            }
        }
        System.out.println("Failed to find the socket for "+address+" at port "+port);
        return null;
    }

    public synchronized int addConnection(Socket socket, Node node){
        TCPConnection conn = new TCPConnection(socket, node);
        connections.add(conn);
        return connections.size()-1;
    }
}
