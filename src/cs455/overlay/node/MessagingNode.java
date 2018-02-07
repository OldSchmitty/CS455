package cs455.overlay.node;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.transport.TCPConnectionsCache;


public class MessagingNode extends Thread implements Node{

    private int nodeNum;
    private String host;
    private int port;


    public MessagingNode(String host, int port){
        this.host = host;
        this.port = port;
        register();
        TCPConnectionsCache cache = new TCPConnectionsCache();
    }

    public void register(){

    }

    public void onEvent(Event event){

    }

    public void setNodeNum(int nodeNum){
        if (nodeNum > 0 && nodeNum < 128){
            this.nodeNum = nodeNum;
        }
        else{
            this.nodeNum = -1;
        }
    }

    public int getNodeNum(){
        return this.nodeNum;
    }

    public void run(){
        try(ServerSocket serverSocket = new ServerSocket(0)){
            while(true) {
                Socket socket = serverSocket.accept();

            }
        }catch(IOException e) {
            System.out.println(e);
        }

    }
}
