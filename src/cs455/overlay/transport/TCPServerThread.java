package cs455.overlay.transport;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.Event;

public class TCPServerThread extends Thread{
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

    public TCPConnection getCacheConn(Socket socket){
        return cache.getBySocket(socket);
    }

    public synchronized byte[] getAddr(){
        return this.serverSocket.getInetAddress().getAddress().clone();
    }

    public void addRoute(int id, InetAddress address, int port, int hops) throws java.io.IOException{
        table.addEntry(id, address, port, hops, this.node);
    }
    public void addRoute(int id, int hops, TCPConnection conn, int port){
        table.addEntry(id, hops, conn, port);
    }

    public void close(){
        done = true;
        try {
            serverSocket.close();
        }catch (java.io.IOException e){
            System.out.println(e);
        }
    }

    public int getServerSocketPort() {
        return serverSocket.getLocalPort();
    }

    public void registryExit(byte[] bytes){
        cache.sendAll(bytes);
    }

    public void run(){
        try{
            while(!done) {
                Socket socket = serverSocket.accept();
                cache.addConnection(socket, node, socket.getPort());
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
