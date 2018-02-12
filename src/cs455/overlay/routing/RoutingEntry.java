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
    private int hops;

    public RoutingEntry(InetAddress address, int port, int hops, Node node) throws java.io.IOException{
        this.port = port;
        this.node = node;
        this.hops = hops;
        Socket socket = new Socket(address, port);
        conn = new TCPConnection(socket, this.node);
    }

    RoutingEntry(int hops, TCPConnection conn){
        this.hops = hops;
        this.conn = conn;
    }


    public void sendMsg(Event event){
        conn.sendMessage(event);
    }

    public Socket getSocket(){
        return conn.getSocket();
    }

    public void close(){
        conn.close();
    }

}
