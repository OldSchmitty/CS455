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
    private InetAddress host;
    private int port;
    protected ServerSocket serverSocket;
    private Socket regSocket;
    private  byte[] IPAddress;
    private TCPServerThread server;
    private TCPConnection regConn;
    private boolean registryUp;
    private int messagesSent = 0;
    private int messagesReceived = 0;
    private int messagesRelayed = 0;
    private long sumOfSent = 0;
    private long sumOfRecieved = 0;


    private synchronized void incrementMessagesSent(){
        messagesSent++;
    }
    private synchronized void incrementMessagesReceived(){
        messagesReceived++;
    }
    private synchronized void incrementMessagesRelayed(){
        messagesRelayed++;
    }
    private synchronized void addToSumOfSent(long num){
        sumOfSent += num;
    }
    private synchronized void addToSumOfReceived(long num){
        sumOfRecieved += num;
    }
    private synchronized int getMessagesSent(){
        return messagesSent;
    }
    private synchronized int getMessagesReceived(){
        return messagesReceived;
    }
    private synchronized int getMessagesRelayed(){
        return messagesRelayed;
    }
    private synchronized long getSumOfSent(){
        return sumOfSent;
    }
    private synchronized long getSumOfReceived(){
        return sumOfRecieved;
    }

    private synchronized void resetCounters(){
        this.messagesSent = 0;
        this.messagesReceived = 0;
        this.messagesRelayed = 0;
        this.sumOfSent = 0;
        this.sumOfRecieved = 0;
    }






    public MessagingNode(String host, int port){
        try {
            this.host = InetAddress.getByName(host);
        }catch(java.net.UnknownHostException e) {
            System.out.println(e);
            System.exit(1);
        }
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
        registryUp = true;
    }

    public void addRegistry(InetAddress host, int port){
        try {
            regSocket = new Socket(host, port);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    public void register(){
        OverlayNodeSendsRegistration msg = new OverlayNodeSendsRegistration(
                regConn.getSocket().getLocalAddress().getAddress(), this.regSocket.getLocalPort());
        if (msg.getType() != -1){
            regConn.sendMessage(msg);
        }
    }

    public void deregister(){
        OverlayNodeSendsDeregistration msg = new OverlayNodeSendsDeregistration(
                regConn.getSocket().getLocalAddress().getAddress(),regSocket.getLocalPort(), this.nodeNum);
        regConn.sendMessage(msg);
    }

    public void onEvent(Event event){
        byte type = event.getType();
        switch(type){
        case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
            setup(event);
            break;
        case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
            setupRoutingTable(event);
            break;
        case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
            try {
                RegistryReportsDeregistrationStatus msg = new RegistryReportsDeregistrationStatus(event.getBytes());
                System.out.println(msg.getInformationString());
                if(msg.getSucessStatus() == -1){
                    this.registryUp = false;
                }
            }catch(java.io.IOException e){
                System.out.println(e);
            }
            finally{
                server.close();
                regConn.close();
            }
            break;
        }
    }

    private void setup(Event event){
        try {
            RegistryReportsRegistrationStatus msg = new RegistryReportsRegistrationStatus(event.getBytes());
            setNodeNum(msg.getSucessStatus());
            System.out.println(msg.getInformationString());
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    private void setNodeNum(int nodeNum){
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

    public boolean getRegistryUp(){
        return this.registryUp;
    }

    private void setupRoutingTable(Event event){
        int successStatus = -1;
        String error = "";
        try {
            RegistrySendsNodeManifest msg = new RegistrySendsNodeManifest(event.getBytes());
            for (int i = 0; i < msg.getRoutingTableSize(); i++){
                int id = msg.getNodeID(i);
                InetAddress addr = InetAddress.getByAddress(msg.getIP(i));
                int port = msg.getPort(i);
                int hops = (int)java.lang.Math.pow(2,i);
                server.addRoute(id, addr, port, hops);
            }
            successStatus = getNodeNum();
        }catch (java.io.IOException e){
            System.out.println(e);
            error = "Node failed to create a socket to another node.";
        }
        NodeReportsOverlaySetupStatus returnMsg = new NodeReportsOverlaySetupStatus(successStatus, error);
        regConn.sendMessage(returnMsg);

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
                if(node.getRegistryUp())
                    node.deregister();

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
