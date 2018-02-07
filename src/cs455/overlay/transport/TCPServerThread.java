package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import cs455.overlay.node.Node;

public class TCPServerThread extends Thread{
    //private Queue<byte[]> queue;
    private TCPConnectionsCache cache;
    private Node node;
    public TCPServerThread(Node node){
        //queue = new Queue();
        cache = new TCPConnectionsCache();
        this.node = node;
        this.run();
    }

    public void run(){
        try(ServerSocket serverSocket = new ServerSocket(0)){
            while(true) {
                Socket socket = serverSocket.accept();
                cache.addConnection(socket, node);
            }
        }catch(IOException e) {
            System.out.println(e);
        }

    }
}
