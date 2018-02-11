package cs455.overlay.node;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.TreeMap;

public class Registry implements Node{
    private int portNum;
    private TCPServerThread server;
    protected ServerSocket serverSocket;
    private Random random;
    private static final int MAX_NODES_REGISTERED = 128;

    public Registry(int portNum){
        this.portNum = portNum;
        random = new Random();
        server = new TCPServerThread(this, serverSocket);
    }

    private void setup() {

    }

    private int generateID(){
        return(random.nextInt(MAX_NODES_REGISTERED-1));
    }

    private int getRandomId(){
        int id = -1;
        if (server.table.getNodeNum() < 128) {
            id = generateID();
            while (server.table.hasEntry(id))
                id = generateID();
        }
        return id;
    }

    private void registerNode(OverlayNodeSendsRegistration msg){
        String error = "";
        int id = getRandomId();
        if (id == -1) {
            error = "Error: Can not add node. 128 Nodes already exist.";
        }
        try{
            InetAddress addr = InetAddress.getByAddress(msg.getIPAddress());
            server.addRoute(id,msg.getPortNum(),addr);
        }catch(java.io.IOException e){
            System.out.println(e);
            error = e.toString();
        }
        RegistryReportsRegistrationStatus reportMsg = new RegistryReportsRegistrationStatus(
                id,server.table.getNodeNum(),error);
        server.table.sendMsg(reportMsg, id);
    }

    public void onEvent(Event event){
        byte type = event.getType();
        try {
            switch (type) {
                case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:

                    OverlayNodeSendsRegistration msg = new OverlayNodeSendsRegistration(event.getBytes());
                    registerNode(msg);
                    break;

            }
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    public static void main(String[] args){

    }


}
