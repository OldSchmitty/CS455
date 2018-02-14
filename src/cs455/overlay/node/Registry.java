package cs455.overlay.node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.ArrayList;


public class Registry implements Node{
    private int portNum;
    private TCPServerThread server;
    private ServerSocket serverSocket;
    private ArrayList<Socket> socketList;
    private Random random;
    private static final int MAX_NODES_REGISTERED = 128;
    private ArrayList<RegistrySendsNodeManifest> msgs;
    private int overlaySuccessCount;
    private StatisticsCollectorAndDisplay stats = new StatisticsCollectorAndDisplay();

    public Registry(int portNum){
        this.portNum = portNum;
        random = new Random();
        try {
            this.serverSocket = new ServerSocket(portNum);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
        server = new TCPServerThread(this, serverSocket);
        overlaySuccessCount = 0;
    }

    public void setupOverlay(int nR) {
        if(server.table.getNodeNum() > 2*nR){
            msgs = server.table.getRouteMsgs(nR);
            if(msgs != null) {
                for (RegistrySendsNodeManifest msg : msgs) {
                    try {
                        server.table.sendMsg(msg.getBytes().clone(), msg.getDestinationID());
                    }catch(java.io.IOException e){
                        System.out.println(e);
                    }
                }
            }
        }
        else{
            System.out.println(new String("There are not enough nodes in the overlay to satisfy nodes > 2*nR"));
        }
    }

    public String testInfo(){
        return server.getAddr().toString();
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

    private void registerNode(OverlayNodeSendsRegistration msg, Socket socket){
        String error = "";
        int id = getRandomId();
        if (id == -1) {
            error = new String("Error: Can not add node. 128 Nodes already exist.");
        }
        try{
            InetAddress addr = InetAddress.getByAddress(msg.getIPAddress());
            TCPConnection msgNodeCon = server.getCacheConn(socket);
            InetAddress conAddr = msgNodeCon.getSocket().getInetAddress();
            if(conAddr.equals(addr)){
                server.addRoute(id, 1,msgNodeCon, msg.getPortNum());
            }
            else{
                error = new String("Error: IP address in Registration message does not match socket address");
                id = -1;
            }
            RegistryReportsRegistrationStatus reportMsg = new RegistryReportsRegistrationStatus(
                    id,server.table.getNodeNum(),error);
            if (id != -1) {
                try {
                    server.table.sendMsg(reportMsg.getBytes(), id);
                }catch (java.io.IOException e){
                    System.out.println(e);
                }
            }
            else{
                try {
                    msgNodeCon.sendMessage(reportMsg.getBytes());
                }catch (java.io.IOException e){
                    System.out.println(e);
                }

            }

        }catch(java.io.IOException e){
            System.out.println(e);
            System.exit(1);
        }
    }

    private synchronized void increaseOverlayCounter(){
        this.overlaySuccessCount++;
    }
    private synchronized int checkCounter(){
        return overlaySuccessCount;
    }
    private synchronized void resetCounter() {this.overlaySuccessCount = 0;}


    public void onEvent(Event event, Socket socket){
        byte type = event.getType();
        try {
            switch (type) {
                case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
                    OverlayNodeSendsRegistration regmsg = new OverlayNodeSendsRegistration(event.getBytes().clone());
                    registerNode(regmsg, socket);
                    break;
                case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                    OverlayNodeSendsDeregistration deregmsg = new OverlayNodeSendsDeregistration(event.getBytes().clone());
                    deregisterNode(deregmsg);
                    break;
                case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
                    synchronized (this) {
                        increaseOverlayCounter();
                        OverlayNodeReportsTaskFinished finishMsg = new OverlayNodeReportsTaskFinished(event.getBytes().clone());
                        System.out.println(new String("Node ") + finishMsg.getNodeID() + new String(" has reported task as finished"));
                        if (checkCounter() == server.table.getNodeNum()) {
                            try {
                                Thread.sleep(15000);
                                System.out.println(new String("Registry Requesting Summary information"));
                                requestSummary();
                            } catch (java.lang.InterruptedException e) {
                                System.out.println(e);
                            }
                        }
                    }
                    break;
                case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                    try{
                        OverlayNodeReportsTrafficSummary summaryMsg = new
                                OverlayNodeReportsTrafficSummary(event.getBytes().clone());
                        System.out.println("Node "+summaryMsg.getNodeID()+" has reported its traffic summary.");
                        synchronized (this){
                            stats.addItem(summaryMsg.getNodeID(), summaryMsg.getPacketsSent(),
                                    summaryMsg.getNumberOfPacketsReceived(), summaryMsg.getPacketsRelayed(),
                                    summaryMsg.getSumOfPacketData(), summaryMsg.getSumOfPacketsReceived());
                            stats.IncrementTotal();
                            if (stats.getTotal() == server.table.getNodeNum()){
                                stats.printStats();
                                stats.reset();
                            }

                        }
                    }catch(java.io.IOException e){
                        System.out.println(e);
                    }

                    break;
                case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
                    NodeReportsOverlaySetupStatus statusMsg = new NodeReportsOverlaySetupStatus(event.getBytes().clone());
                    System.out.println(statusMsg.getInformationString());
                    if (statusMsg.getSuccessStatus() == -1){
                        System.out.println(new String("A node failed to setup the overlay: "));
                        System.out.println(new String("Please exit registry and all nodes and try again."));
                    }
                    else {
                        increaseOverlayCounter();
                    }
                    if(checkCounter() == server.table.getNodeNum()){
                        System.out.println(new String("All nodes in registry have successfully set up the overlay."));
                        resetCounter();
                    }
                    break;
            }
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    private void requestSummary(){
        RegistryRequestsTrafficSummary msg = new RegistryRequestsTrafficSummary();
        try {
            server.table.sendAll(msg.getBytes().clone());
        }catch (java.io.IOException e){
            System.out.println(e);
        }

    }

    public void listRoutingTables(){
        try {
            for (RegistrySendsNodeManifest msg : msgs) {
                System.out.println(new String("Routing table for Node: ") + msg.getDestinationID());
                for (int i = 0; i < msg.getRoutingTableSize(); i++) {
                    System.out.println(new String("Entry " + (i + 1) + ":  IP = " + InetAddress.getByAddress(msg.getIP(i)) +
                            "        port = " + msg.getPort(i) + "         id = " + msg.getNodeID(i)));
                }
                System.out.println();
                System.out.println();
                System.out.println();
            }
        }catch (java.net.UnknownHostException e){
            System.out.println(e);
        }
    }

    public void deregisterNode(OverlayNodeSendsDeregistration msg){
        int id = msg.getNodeID();
        String error = new String("");
        RegistryReportsDeregistrationStatus deregmsg;
        int successStatus = -1;
        if (!server.table.hasEntry(id)){
            error =  new String("Error: Node "+msg.getNodeID()+" is not in the registry.");
            successStatus = -1;
        }
        else{
            try {
                InetAddress msgAddr = InetAddress.getByAddress(msg.getIPAddress());
                InetAddress socketAddr = server.table.getSocket(id).getInetAddress();

                if (msgAddr.equals(socketAddr)) {
                    successStatus = id;
                } else {
                    successStatus = -1;
                    error = new String("Error: Node IP Address in msg does not match Socket Address.");
                }
            }catch(java.io.IOException e){
                System.out.println(e);
            }
        }
        deregmsg = new RegistryReportsDeregistrationStatus(successStatus, error);
        try {
            server.table.sendMsg(deregmsg.getBytes().clone(),id);
        }catch (java.io.IOException e){
            System.out.println(e);
        }
        if (successStatus != -1) {
            server.table.removeEntry(id);
        }
    }

    public void close(){
        server.close();
    }

    public void taskStart(int num){
        RegistryRequestsTaskInitiate msg = new RegistryRequestsTaskInitiate(num);
        try {
            server.table.sendAll(msg.getBytes().clone());
        }catch (java.io.IOException e){
            System.out.println(e);
        }

    }

    public void listMessagingNodes(){
        server.table.printTable();
    }


    public static void main(String[] args){
        if (args.length == 1){
            try{
                int portNum = Integer.parseInt(args[0]);
                Registry registry = new Registry(portNum);
                registry.testInfo();
                Scanner scanner = new Scanner(System.in);
                String inputString = scanner.next();
                boolean start = false;
                while (!(inputString.equals("exit"))){
                    switch (inputString){
                        case "list-messaging-nodes":
                            registry.listMessagingNodes();
                            break;

                        case "setup-overlay":
                            inputString = scanner.next();
                            int nR = Integer.parseInt(inputString);
                            if (nR<1){
                                System.out.println(new String("Error: nR must be greater than 0."));
                            }
                            else{
                                registry.setupOverlay(nR);
                            }
                            break;

                        case "list-routing-tables":
                            registry.listRoutingTables();
                            break;

                        case "start":
                            inputString = scanner.next();
                            int numOfPackets = Integer.parseInt(inputString);
                            registry.taskStart(numOfPackets);
                            break;

                        default:
                            System.out.println(
                                    new String("Error: invalid command, try again"));
                    }
                    inputString = scanner.next();
                }
                registry.close();

            }catch (NumberFormatException e){
                System.out.println(e);
            }
        }
        else{
            System.out.println((new String("Error: Incorrect number of arguments "+args.length+
                    " you must specify a port-num.")));
        }

    }


}
