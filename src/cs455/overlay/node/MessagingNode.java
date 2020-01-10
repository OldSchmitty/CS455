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
    private int[] nodeIDs;
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
            System.out.println(new String("Error: Could not connect to "+host+" at port number "+port));
            System.exit(1);
        }
        this.regConn = new TCPConnection(this.regSocket, this, this.port);
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
                regConn.getSocket().getLocalAddress().getAddress(), server.getServerSocketPort());
        if (msg.getType() != -1){
            try {
                regConn.sendMessage(msg.getBytes().clone());
            }catch (java.io.IOException e){
                System.out.println(e);
            }
        }
    }

    public void deregister(){
        OverlayNodeSendsDeregistration msg = new OverlayNodeSendsDeregistration(
                regConn.getSocket().getLocalAddress().getAddress(),regSocket.getLocalPort(), this.nodeNum);
        try {
            regConn.sendMessage(msg.getBytes().clone());
        }catch (java.io.IOException e){
            System.out.println(e);
        }
    }

    public void onEvent(Event event, Socket socket){

        byte type = event.getType();
        try {
            byte[] bytes = event.getBytes().clone();
            switch (type) {
                case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
                    setup(bytes);
                    break;
                case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
                    setupRoutingTable(bytes);
                    break;
                case Protocol.OVERLAY_NODE_SENDS_DATA:
                    receiveData(bytes);
                    break;
                case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                    sendSummary();
                    break;
                case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
                    try {
                        RegistryRequestsTaskInitiate taskMsg = new RegistryRequestsTaskInitiate(bytes);
                        System.out.println(new String("Task initiating, sending ") + taskMsg.getNumberOfPackets() + new String(" packets"));
                        int num = taskMsg.getNumberOfPackets();
                        for (int i = 0; i < num; i++) {
                            long sum = server.table.startTask(nodeIDs, nodeNum);
                            addToSumOfSent(sum);
                            incrementMessagesSent();
                        }
                        sendTaskFinished();
                        System.gc();
                    } catch (java.io.IOException e) {
                        System.out.println(e);
                    }
                    break;
                case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                    try {
                        RegistryReportsDeregistrationStatus msg = new RegistryReportsDeregistrationStatus(bytes);
                        System.out.println(msg.getInformationString());
                        if (msg.getSucessStatus() == -1) {
                            this.registryUp = false;
                        }
                    } catch (java.io.IOException e) {
                        System.out.println(e);
                    } finally {
                        server.close();
                        regConn.close();
                    }
                    break;
            }
        }catch(java.io.IOException e){
                System.out.println(e);
            }

    }

    private void receiveData(byte[] bytes){
        try{
            OverlayNodeSendsData msg = new OverlayNodeSendsData(bytes.clone());
            if(msg.getDestinationID() == nodeNum){
                incrementMessagesReceived();
                addToSumOfReceived(msg.getPayload());
            }
            else{
                incrementMessagesRelayed();
                msg.addTrace(nodeNum);
                server.table.sendMsg(bytes.clone(), msg.getDestinationID());
            }
        }catch (java.io.IOException e){
            System.out.println(e);
        }
    }

    private void setup(byte[] bytes){
        try {
            RegistryReportsRegistrationStatus msg = new RegistryReportsRegistrationStatus(bytes.clone());
            setNodeNum(msg.getSucessStatus());
            System.out.println(msg.getInformationString());
            server.table.setID(nodeNum);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    private void setNodeNum(int nodeNum){
        if (nodeNum >= 0 && nodeNum < 128){
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

    private void setupRoutingTable(byte[] bytes){
        int successStatus = -1;
        String error = "";
        try {
            RegistrySendsNodeManifest msg = new RegistrySendsNodeManifest(bytes.clone());
            for (int i = 0; i < msg.getRoutingTableSize(); i++){
                int id = msg.getNodeID(i);
                InetAddress addr = InetAddress.getByAddress(msg.getIP(i));
                int port = msg.getPort(i);
                int hops = (int)java.lang.Math.pow(2,i);
                server.addRoute(id, addr, port, hops);
            }
            successStatus = getNodeNum();
            nodeIDs = msg.getListOfNodeIDs();

        }catch (java.io.IOException e){
            System.out.println(e);
            error = new String("Node failed to create a socket to another node.");
        }
        NodeReportsOverlaySetupStatus returnMsg = new NodeReportsOverlaySetupStatus(successStatus, error);
        try {
            regConn.sendMessage(returnMsg.getBytes().clone());
        }catch (java.io.IOException e){
            System.out.println(e);
        }

    }

    private void sendTaskFinished(){
        OverlayNodeReportsTaskFinished msg = new OverlayNodeReportsTaskFinished(
                regConn.getSocket().getLocalAddress().getAddress(), this.regSocket.getLocalPort(), nodeNum);
        try {
            regConn.sendMessage(msg.getBytes().clone());
        }catch (java.io.IOException e){
            System.out.println(e);
        }
    }

    private void sendSummary(){
        synchronized (this) {
            OverlayNodeReportsTrafficSummary msg = new OverlayNodeReportsTrafficSummary(
                    nodeNum, getMessagesSent(), getMessagesRelayed(), getSumOfSent(),
                    getMessagesReceived(), getSumOfReceived());
            try {
                regConn.sendMessage(msg.getBytes().clone());
            } catch (java.io.IOException e) {
                System.out.println(e);
            }
            resetCounters();
        }
    }


    public static void main(String[] args){
        if (args.length == 2){
            try{
                String registryHost = args[0];
                int portNum = Integer.parseInt(args[1]);
                MessagingNode node = new MessagingNode(registryHost, portNum);
                Scanner scanner = new Scanner(System.in);
                String inputString = scanner.next();
                while (!(inputString.equals(new String("exit-overlay")))){
                    switch (inputString){
                        case "print-counters-and-diagnostics":
                            synchronized (node){
                                System.out.println("Messages Sent: "+node.getMessagesSent());
                                System.out.println("Messages Received: "+node.getMessagesReceived());
                                System.out.println("Messages Relayed: "+node.getMessagesRelayed());
                                System.out.println("Messages Sent Sum: "+node.getSumOfSent());
                                System.out.println("Messages Received Sum: "+node.getSumOfReceived());
                                System.out.println();
                            }
                            break;
                        default:
                            System.out.println(
                                    new String("Error: Commands are <print-counters-and-diagnostis> or <exit-overlay>."));

                    }
                    inputString = scanner.next();
                }
                if(node.getRegistryUp())
                    node.deregister();

            }catch (NumberFormatException e){
                System.out.println(e);
            }
        }
        else{
            System.out.println(new String("Error: Incorrect number of arguments ")+args.length+
                    new String(" you must specify a registry-host and a port-num."));
        }
    }

}
