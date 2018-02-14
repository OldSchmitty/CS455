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
    private int ID;

    public RoutingEntry(InetAddress address, int port, int hops, Node node, int ID) throws java.io.IOException{
        this.port = port;
        this.node = node;
        this.hops = hops;
        Socket socket = new Socket(address, port);
        conn = new TCPConnection(socket, this.node, port);
        this.ID = ID;
    }

    RoutingEntry(int hops, TCPConnection conn, int ID, int port){
        this.hops = hops;
        this.conn = conn;
        this.ID = ID;
        this.port = port;
    }


    public void sendMsg(byte[] bytes){
        conn.sendMessage(bytes);
    }

    public Socket getSocket(){
        return conn.getSocket();
    }

    public int getPort() {
        return port;
    }

    public void close(){
        conn.close();
    }

    public int getID(){
        return ID;
    }

}
