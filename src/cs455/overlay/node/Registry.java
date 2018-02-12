package cs455.overlay.node;
import cs455.overlay.transport.TCPConnectionsCache;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

public class Registry implements Node{
    private int portNum;
    private TCPServerThread server;
    private ServerSocket serverSocket;
    private Random random;
    private static final int MAX_NODES_REGISTERED = 128;

    public Registry(int portNum){
        this.portNum = portNum;
        random = new Random();
        try {
            this.serverSocket = new ServerSocket(portNum);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
        server = new TCPServerThread(this, serverSocket);
    }

    private void setup() {

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

    private void registerNode(OverlayNodeSendsRegistration msg){
        String error = "";
        int id = getRandomId();
        if (id == -1) {
            error = "Error: Can not add node. 128 Nodes already exist.";
        }
        try{
            InetAddress addr = InetAddress.getByAddress(msg.getIPAddress());
            Socket msgNodeSocket = server.getCacheSocket(addr,msg.getPortNum());
            server.addRoute(id,msgNodeSocket);
        }catch(java.io.IOException e){
            System.out.println(e);
            System.exit(1);
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
                case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                    OverlayNodeSendsDeregistration msg = new OverlayNodeSendsDeregistration(event.getBytes());
                    deregisterNode(msg);
            }
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    public void deregisterNode(OverlayNodeSendsDeregistration msg){
        if(!server.table.hasEntry(msg.getNodeID())){
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
                while (!(inputString.equals("exit-overlay"))){
                    switch (inputString){
                        case "list-messaging-nodes":
                            System.out.println("TO-DO : list-messaging-Nodes");
                            break;

                        case "setup-overlay":
                            inputString = scanner.next();
                            int nR = Integer.parseInt(inputString);
                            System.out.println("TO-DO : setup-overlay with nR = " +nR);
                            break;

                        case "list-routing-tables":
                            System.out.println("TO-DO : list-routing-tables");
                            break;

                        case "start":
                            inputString = scanner.next();
                            int numRoutingTableEnties = Integer.parseInt(inputString);
                            start = true;
                            System.out.println("TO-DO : Start with "+numRoutingTableEnties+" entries");
                            break;

                        default:
                            System.out.println(
                                    "Error: Commands are <print-counters-and-diagnostis> or <exit-overlay>.");
                    }
                    inputString = scanner.next();
                }

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
