package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;

public class TCPServerThread extends Thread{
    //private Queue<byte[]> queue;
    private TCPConnectionsCache cache;
    private Node node;
    private ServerSocket serverSocket;
    public TCPServerThread(Node node, ServerSocket serverSocket){
        //queue = new Queue()
        this.serverSocket = serverSocket;
        cache = new TCPConnectionsCache();
        this.node = node;
        this.start();
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
