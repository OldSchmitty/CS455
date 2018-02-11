package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.routing.RoutingTable;

public class TCPServerThread extends Thread{
    //private Queue<byte[]> queue;
    private TCPConnectionsCache cache;
    private Node node;
    private ServerSocket serverSocket;
    public RoutingTable table;

    public TCPServerThread(Node node, ServerSocket serverSocket){
        //queue = new Queue()
        this.serverSocket = serverSocket;
        this.node = node;
        cache = new TCPConnectionsCache(this.node);
        this.table = new RoutingTable(this.node);
        this.start();
    }

    public synchronized byte[] getAddr(){
        return this.serverSocket.getInetAddress().getAddress();
    }

    public void addRoute(int id, int port, InetAddress address) throws java.io.IOException{
        table.addEntry(id, address, port, this.node);
    }

    public void run(){
        try{
            serverSocket = new ServerSocket(0);
            while(true) {
                Socket socket = serverSocket.accept();
                cache.addConnection(socket, node);
            }
        }catch(IOException e) {
            System.out.println(e);
        }

    }
}
