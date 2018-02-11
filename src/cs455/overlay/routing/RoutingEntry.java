package cs455.overlay.routing;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

import java.net.InetAddress;
import java.net.Socket;

public class RoutingEntry {
    private int port;
    private TCPConnection conn;
    private Node node;

    public RoutingEntry(InetAddress address, int port, Node node) throws java.io.IOException{
        this.port = port;
        this.node = node;
        Socket socket = new Socket(address, port);
        conn = new TCPConnection(socket, this.node);
    }

    RoutingEntry(int id, Socket socket, Node node){
        this.node = node;
        conn = new TCPConnection(socket, this.node);
    }

    public void sendMsg(Event event){
        conn.sendMessage(event);
    }

    public void close(){
        conn.close();
    }

}
