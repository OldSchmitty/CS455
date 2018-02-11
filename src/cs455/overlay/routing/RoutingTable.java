package cs455.overlay.routing;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

import java.net.InetAddress;
import java.util.TreeMap;

public class RoutingTable {
    private Node node;
    private TreeMap<Integer, RoutingEntry>  table;

    public RoutingTable(Node node){
        this.table = new TreeMap<>();
        this.node = node;
    }

    public synchronized void addEntry (int id, InetAddress address, int port, Node node)throws java.io.IOException{
        RoutingEntry newEntry = new RoutingEntry(address, port, node);
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

    public void sendMsg(Event event, int id){
        table.get(id).sendMsg(event);
    }

    public void close(){
        for (RoutingEntry r : table.values()){
            r.close();
        }
    }


}
