package cs455.overlay.node;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.TreeMap;

public class Registry implements Node{
    private int portNum;
    private TCPServerThread server;
    protected ServerSocket serverSocket;
    private Random random;
    private TreeMap<Integer,Socket> registeredNodeMap;

    public Registry(int portNum){
        this.portNum = portNum;
        random = new Random();
        registeredNodeMap = new TreeMap<>();
        server = new TCPServerThread(this, serverSocket);
    }

    private void setup() {

    }

    private int generateID(){
        return(random.nextInt(127));
    }

    private void registerNode(Socket socket){
        if (registeredNodeMap.size() != 128) {
            int id = generateID();
            while (registeredNodeMap.get(id) != null)
                id = generateID();
            registeredNodeMap.put(id, socket);
            sendRegistrationStatus(id);
        }
        else {
            System.out.println("Error: Can not add node. 128 Nodes already exist.");
        }
    }

    private void sendRegistrationStatus(int id){
        
    }

    public void onEvent(Event event){

    }


}
