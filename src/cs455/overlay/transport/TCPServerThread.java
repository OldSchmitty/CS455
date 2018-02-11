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
    private volatile boolean done;

    public TCPServerThread(Node node, ServerSocket serverSocket){
        done = false;
        this.serverSocket = serverSocket;
        this.node = node;
        cache = new TCPConnectionsCache(this.node);
        this.table = new RoutingTable(this.node);
        this.start();
    }
    public Socket getCacheSocket(InetAddress address, int port){
        return cache.getSocket(address, port);
    }

    public synchronized byte[] getAddr(){
        return this.serverSocket.getInetAddress().getAddress();
    }

    public void addRoute(int id, int port, InetAddress address) throws java.io.IOException{
        table.addEntry(id, address, port, this.node);
    }
    public void addRoute(int id, Socket socket){
        table.addEntry(id, socket);
    }

    public void close(){
        done = true;
        try {
            serverSocket.close();
        }catch (java.io.IOException e){
            System.out.println(e);
        }
    }

    public void run(){
        try{
            while(!done) {
                Socket socket = serverSocket.accept();
                cache.addConnection(socket, node);
            }
        }catch(java.io.IOException e) {
            if(!(e instanceof java.net.SocketException))
                System.out.println(e);
        }
        finally {
            cache.close();
            table.close();
        }
    }
}
