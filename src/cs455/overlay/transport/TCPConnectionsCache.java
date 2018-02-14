package cs455.overlay.transport;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import cs455.overlay.node.Node;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.Event;

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

    public synchronized TCPConnection getBySocket(Socket socket){
        Socket rSocket;
        for(TCPConnection conn : connections){
            rSocket = conn.getSocket();
            if(rSocket == socket){
                return conn;
            }
        }
        System.out.println("Failed to find a socket connection, reference was lost.");
        return null;
    }

    public synchronized int addConnection(Socket socket, Node node, int port){
        TCPConnection conn = new TCPConnection(socket, node, port);
        connections.add(conn);
        return connections.size()-1;
    }
    public synchronized void sendAll(Event msg){
        for (TCPConnection conn : connections){
            conn.sendMessage(msg);
        }
    }
}
