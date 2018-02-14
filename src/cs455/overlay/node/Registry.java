package cs455.overlay.node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
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
            listRoutingTables();
            if(msgs != null) {
                for (RegistrySendsNodeManifest msg : msgs) {
                    server.table.sendMsg(msg, msg.getDestinationID());
                }
            }
        }
        else{
            System.out.println("There are not enough nodes in the overlay to satisfy nodes > 2*nR");
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
            error = "Error: Can not add node. 128 Nodes already exist.";
        }
        try{
            InetAddress addr = InetAddress.getByAddress(msg.getIPAddress());
            TCPConnection msgNodeCon = server.getCacheConn(socket);
            InetAddress conAddr = msgNodeCon.getSocket().getInetAddress();
            if(conAddr.equals(addr)){
                server.addRoute(id, 1,msgNodeCon, msg.getPortNum());
            }
            else{
                error = "Error: IP address in Registration message does not match socket address";
                id = -1;
            }
            RegistryReportsRegistrationStatus reportMsg = new RegistryReportsRegistrationStatus(
                    id,server.table.getNodeNum(),error);
            if (id != -1) {
                server.table.sendMsg(reportMsg, id);
            }
            else{
                msgNodeCon.sendMessage(reportMsg);
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
                    OverlayNodeSendsRegistration regmsg = new OverlayNodeSendsRegistration(event.getBytes());
                    registerNode(regmsg, socket);
                    break;
                case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                    OverlayNodeSendsDeregistration deregmsg = new OverlayNodeSendsDeregistration(event.getBytes());
                    deregisterNode(deregmsg);
                    break;
                case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
                    synchronized (this) {
                        increaseOverlayCounter();
                        OverlayNodeReportsTaskFinished finishMsg = new OverlayNodeReportsTaskFinished(event.getBytes());
                        System.out.println("Node " + finishMsg.getNodeID() + " has reported task as finished");
                        if (checkCounter() == server.table.getNodeNum()) {
                            try {
                                Thread.sleep(15000);
                                System.out.println("Registry Requesting Summary information");
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
                                OverlayNodeReportsTrafficSummary(event.getBytes());
                        System.out.println("Got summary from Node" + summaryMsg.getNodeID());
                    }catch(java.io.IOException e){
                        System.out.println(e);
                    }

                    break;
                case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
                    NodeReportsOverlaySetupStatus statusMsg = new NodeReportsOverlaySetupStatus(event.getBytes());
                    System.out.println(statusMsg.getInformationString());
                    if (statusMsg.getSuccessStatus() == -1){
                        System.out.println("A node failed to setup the overlay: ");
                        System.out.println("Please exit registry and all nodes and try again.");
                    }
                    else {
                        increaseOverlayCounter();
                    }
                    if(checkCounter() == server.table.getNodeNum()){
                        System.out.println("All nodes in registry have successfully set up the overlay.");
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
        server.table.sendAll(msg);
    }

    public void listRoutingTables(){
        try {
            for (RegistrySendsNodeManifest msg : msgs) {
                System.out.println("Routing table for Node: " + msg.getDestinationID());
                for (int i = 0; i < msg.getRoutingTableSize(); i++) {
                    System.out.println("Entry " + (i + 1) + ":  IP = " + InetAddress.getByAddress(msg.getIP(i)) +
                            "        port = " + msg.getPort(i) + "         id = " + msg.getNodeID(i));
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
        String error = "";
        RegistryReportsDeregistrationStatus deregmsg;
        int successStatus = -1;
        if (!server.table.hasEntry(id)){
            error = "Error: Node "+msg.getNodeID()+" is not in the registry.";
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
                    error = "Error: Node IP Address in msg does not match Socket Address.";
                }
            }catch(java.io.IOException e){
                System.out.println(e);
            }
        }
        deregmsg = new RegistryReportsDeregistrationStatus(successStatus, error);
        server.table.sendMsg(deregmsg,id);
        if (successStatus != -1) {
            server.table.removeEntry(id);
        }
    }

    public void close(){
        server.close();
    }

    public void taskStart(int num){
        RegistryRequestsTaskInitiate msg = new RegistryRequestsTaskInitiate(num);
        server.table.sendAll(msg);
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
                            System.out.println("TO-DO : list-messaging-Nodes");
                            break;

                        case "setup-overlay":
                            inputString = scanner.next();
                            int nR = Integer.parseInt(inputString);
                            if (nR<1){
                                System.out.println("Error: nR must be greater than 0.");
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
                                    "Error: Commands are <print-counters-and-diagnostis> or <exit-overlay>.");
                    }
                    inputString = scanner.next();
                }
                registry.close();

            }catch (NumberFormatException e){
                System.out.println(e);
            }
        }
        else{
            System.out.println("Error: Incorrect number of arguments "+args.length+
                    " you must specify a port-num.");
        }

    }


}
