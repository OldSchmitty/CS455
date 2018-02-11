package cs455.overlay.node;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.TCPConnectionsCache;

import java.net.InetAddress;


public class MessagingNode implements Node{

    private int nodeNum;
    private String host;
    private int port;
    private int idNum;
    protected ServerSocket serverSocket;
    private Socket regSocket;
    private TCPConnectionsCache cache;
    private  byte[] IPAddress;
    private TCPServerThread server;
    private TCPConnection regConn;

    public int getIdNum(){
        return this.idNum;
    }

    public MessagingNode(String host, int port){
        this.host = host;
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(0);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
        this.server = new TCPServerThread(this, serverSocket);
        addRegistry(this.host, this.port);
        this.IPAddress = server.getAddr();
        if(regSocket == null){
            System.out.println("Error: Could not connect to "+host+" at port number "+port);
            System.exit(1);
        }
        this.regConn = new TCPConnection(this.regSocket, this);
        register();
    }

    public void addRegistry(String host, int port){
        try {
            regSocket = new Socket(host, port);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    public void register(){
        OverlayNodeSendsRegistration msg = new OverlayNodeSendsRegistration(this.IPAddress, this.regSocket.getPort());
        if (msg.getType() != -1){
            regConn.sendMessage(msg);
        }
    }

    public void onEvent(Event event){
        try{
            byte type = event.getType();
            switch(type){
            case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
                RegistryReportsRegistrationStatus msg = new RegistryReportsRegistrationStatus(event.getBytes());
                this.idNum = msg.getSucessStatus();
                System.out.println(msg.getInformationString());
                break;
            }

        }catch(java.io.IOException e){
            System.out.println(e);
        }

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

    public static void main(String[] args){
        if (args.length == 2){
            try{
                String registryHost = args[0];
                int portNum = Integer.parseInt(args[1]);
                MessagingNode node = new MessagingNode(registryHost, portNum);
                Scanner scanner = new Scanner(System.in);
                String inputString = scanner.next();
                while (!(inputString.equals("exit-overlay"))){
                    switch (inputString){
                        case "print-counters-and-diagnostics":
                            System.out.println("TO-DO : Diagnostics print function here");
                            break;
                        default:
                            System.out.println(
                                    "Error: Commands are <print-counters-and-diagnostis> or <exit-overlay>.");
                        inputString = scanner.next();
                    }
                }

            }catch (NumberFormatException e){
                System.out.println(e);
            }
        }
        else{
            System.out.println("Error: Incorrect number of arguments "+args.length+
                    " you must specify a registry-host and a port-num.");
        }
    }

}
