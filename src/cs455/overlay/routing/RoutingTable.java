package cs455.overlay.routing;
import cs455.overlay.node.Node;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.ArrayList;

public class RoutingTable {
    private Node node;
    private TreeMap<Integer, RoutingEntry>  table;

    public RoutingTable(Node node){
        this.table = new TreeMap<>();
        this.node = node;
    }

    public synchronized void addEntry (int id, InetAddress address, int port, int hops, Node node)throws java.io.IOException{
        RoutingEntry newEntry = new RoutingEntry(address, port, hops, node, id);
        table.put(id, newEntry);
    }

    public synchronized void addEntry(int id, int hops, TCPConnection conn){
        RoutingEntry newEntry = new RoutingEntry(hops, conn, id);
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

    public synchronized boolean sendMsg(Event event, int id){
        if (table.get(id) != null) {
            table.get(id).sendMsg(event);
            return true;
        }

        else{
            RoutingEntry route = null;
            RoutingEntry currentRoute = null;
            int currentDistance = -1;
            int distance = -1;
            Collection<RoutingEntry> c = table.values();
            Iterator<RoutingEntry> itr = c.iterator();
            if (itr.hasNext()){
                currentRoute = itr.next();
                currentDistance = id - currentRoute.getID();
            }
            while (itr.hasNext()){
                route = itr.next();
                distance = id-route.getID();
                if (distance > 0 && currentDistance > 0){
                    if (distance < currentDistance){
                        currentDistance = distance;
                        currentRoute = route;
                    }
                    else if (distance > 0 && currentDistance < 0){
                        currentDistance = distance;
                        currentRoute = route;
                    }
                }
            }
            currentRoute.sendMsg(event);
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

    public synchronized void sendAll(Event msg){
        for(RoutingEntry entry : table.values()){
            entry.sendMsg(msg);
        }
    }

    private RegistrySendsNodeManifest makeRouteMsg(ArrayList<RoutingEntry> routes, int[] idArray){
        RegistrySendsNodeManifest msg = new RegistrySendsNodeManifest(routes, idArray);
        return msg;
    }


    public synchronized ArrayList<RegistrySendsNodeManifest> getRouteMsgs(int nR){
        int[] idArray = new int[table.size()];
        int index = 0;
        for(RoutingEntry re : table.values()){
            idArray[index] = re.getID();
            index ++;
        }
        Collection<RoutingEntry> c = table.values();
        Iterator<RoutingEntry> currentNode = c.iterator();
        RoutingEntry route = null;
        RoutingEntry currentEntry;
        ArrayList<RegistrySendsNodeManifest> msgList = new ArrayList<RegistrySendsNodeManifest>();
        while (currentNode.hasNext()) {
            ArrayList<RoutingEntry> routeList = new ArrayList<RoutingEntry>();
            currentEntry = currentNode.next();
            Iterator<RoutingEntry> itr = c.iterator();
            route = itr.next();
            while (!route.equals(currentEntry)){
                route = itr.next();
            }
            int count = 1;
            while (count <= nR) {
                int next = (int) java.lang.Math.pow(2, count) - 1;
                for (int i = 0; i < next; i++) {
                    if (itr.hasNext()) {
                        route = itr.next();
                    } else {
                        itr = c.iterator();
                        route = itr.next();
                    }
                }
                if (!currentNode.equals(itr)) {
                    routeList.add(route);
                } else {
                    System.out.println(
                            "A routing table of " + nR + " size results in a node including itself in its routing table.");
                    return null;
                }
                count++;

            }
            msgList.add(makeRouteMsg(routeList, idArray));
        }

        return msgList;
    }

}
