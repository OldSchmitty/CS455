package cs455.overlay.routing;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

import java.net.InetAddress;
import java.net.Socket;
import java.util.TreeMap;

public class RoutingTable {
    private Node node;
    private TreeMap<Integer, RoutingEntry>  table;

    public RoutingTable(Node node){
        this.table = new TreeMap<>();
        this.node = node;
    }

    public synchronized void addEntry (int id, InetAddress address, int port, int hops, Node node)throws java.io.IOException{
        RoutingEntry newEntry = new RoutingEntry(address, port, hops, node);
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

    public synchronized void sendMsg(Event event, int id){
        table.get(id).sendMsg(event);
    }

    public synchronized void close(){
        for (RoutingEntry r : table.values()){
            r.close();
        }
    }

    public synchronized void addEntry(int id, int hops, TCPConnection conn){
            RoutingEntry newEntry = new RoutingEntry(hops, conn);
            table.put(id, newEntry);
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

    public synchronized void sendAll(Event msg){
        for(RoutingEntry entry : table.values()){
            entry.sendMsg(msg);
        }
    }


}
