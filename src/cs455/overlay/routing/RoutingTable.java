package cs455.overlay.routing;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class RoutingTable {
    private Node node;
    private TreeMap<Integer, RoutingEntry>  table;
    private Random random;
    private int nodeID;

    public RoutingTable(Node node){
        this.table = new TreeMap<>();
        this.node = node;
        random = new Random();
    }

    public void setID(int num){
        this.nodeID = num;
    }

    public synchronized void addEntry (int id, InetAddress address, int port, int hops, Node node)throws java.io.IOException{
        RoutingEntry newEntry = new RoutingEntry(address, port, hops, node, id);
        table.put(id, newEntry);
    }

    public synchronized void addEntry(int id, int hops, TCPConnection conn, int port){
        RoutingEntry newEntry = new RoutingEntry(hops, conn, id, port);
        table.put(id, newEntry);
    }


    public synchronized int getNodeNum(){
        return table.size();
    }

    public synchronized boolean hasEntry(int id){
        if (table.get(id) == null){
            return false;
        }
        else{
            return true;
        }
    }

    public synchronized boolean sendMsg(byte[] bytes, int id){
        if (table.get(id) != null) {
            table.get(id).sendMsg(bytes.clone());
            return true;
        }

        else{
            RoutingEntry currentRoute = null;
            int currentDistance = -1;
            int distance = -1;
            Set<Integer> set = table.keySet();
            Integer[] array = set.toArray(new Integer[set.size()]);
            int itr = 0;
            int currentKey = array[itr];
            currentDistance = id - currentKey;
            currentRoute = table.get(currentKey);
            itr++;
            while (itr < array.length){
                distance = id - array[itr];
                currentKey = array[itr];
                if (distance > 0 && currentDistance > 0) {
                    if (distance < currentDistance) {
                        currentDistance = distance;
                        currentRoute = table.get(currentKey);
                    }
                }
                else if (distance > 0 && currentDistance < 0){
                    currentDistance = distance;
                    currentRoute = table.get(currentKey);
                }
                else if (distance < 0 && currentDistance < 0){
                    if (distance < currentDistance){
                        currentRoute = table.get(currentKey);
                        currentDistance = distance;
                    }
                }
                itr++;
            }
            currentRoute.sendMsg(bytes.clone());
        }
        return false;
    }

    public synchronized void close(){
        for (RoutingEntry r : table.values()){
            r.close();
        }
    }

    public synchronized int removeEntry(int id){
        if (table.get(id) == null){
            return -1;
        }
        else{
            table.remove(id);
            return id;
        }
    }

    public synchronized Socket getSocket(int id){
        return table.get(id).getSocket();
    }

    public synchronized void sendAll(byte[] bytes){
        for(RoutingEntry entry : table.values()){
            entry.sendMsg(bytes.clone());
        }
    }

    private RegistrySendsNodeManifest makeRouteMsg(ArrayList<RoutingEntry> routes, int[] idArray, int id){
        RegistrySendsNodeManifest msg = new RegistrySendsNodeManifest(routes, idArray, id);
        return msg;
    }


    public synchronized ArrayList<RegistrySendsNodeManifest> getRouteMsgs(int nR){
        int[] idArray = new int[table.size()];
        int index = 0;
        for(int key : table.keySet()){
            idArray[index] = table.get(key).getID();
            index ++;
        }
        Set<Integer> set = table.keySet();
        Integer[] array = set.toArray(new Integer[set.size()]);

        int key = -1;
        int currentKey;
        ArrayList<RegistrySendsNodeManifest> msgList = new ArrayList<RegistrySendsNodeManifest>();
        for(int currentIndex = 0; currentIndex < array.length; currentIndex++) {
            ArrayList<RoutingEntry> routeList = new ArrayList<RoutingEntry>();
            currentKey = array[currentIndex];
            int count = 1;
            while (count <= nR) {
                int itr = currentIndex;
                int next = (int) java.lang.Math.pow(2, count - 1);
                for (int i = 0; i < next; i++) {
                    itr++;
                    if (itr == array.length){
                        itr = 0;
                    }
                }
                key = array[itr];
                if (key != currentKey) {
                    routeList.add(table.get(key));
                } else {
                    System.out.println(
                            new String("A routing table of " + nR + " size results in a node including itself in its routing table."));
                    return null;
                }
                count++;

            }
            msgList.add(makeRouteMsg(routeList, idArray, currentKey));
        }

        return msgList;
    }

    public long startTask(int num, int[] nodeIDs, int nodeNum){
        long sumOfSent = 0;
        byte[] bytes;
        for (int i = 0; i < num; i++){
            int payload = random.nextInt();
            int destination = nodeIDs[random.nextInt(nodeIDs.length)];
            while (destination == nodeNum){
                destination = nodeIDs[random.nextInt(nodeIDs.length)];
            }
            OverlayNodeSendsData msg = new OverlayNodeSendsData(destination,nodeNum,payload);
            try {
                 bytes= msg.getBytes().clone();
                sendMsg(bytes, destination);
                sumOfSent += payload;
            }catch (java.io.IOException e){
                System.out.println(e);
            }
        }
        System.gc();
        return sumOfSent;
    }

    public void printTable(){
        for(int key : table.keySet()){
            RoutingEntry r = table.get(key);
            System.out.println("Address: "+r.getSocket().getInetAddress().getAddress()
                    +"     Port: "+r.getPort()+"     ID: "+r.getID());
        }
    }

}
