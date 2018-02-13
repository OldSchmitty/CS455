package cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;
import cs455.overlay.routing.RoutingEntry;

public class RegistrySendsNodeManifest implements Event{
    private byte msgType = Protocol.REGISTRY_SENDS_NODE_MANIFEST;
    private byte routingTableSize;
    private ArrayList<RouteInfo> routes;
    private byte numberOfNodeIDs;
    private int[] listOfNodeIDs;
    private int destinationID;

    public byte getType(){
        return msgType;
    }
    public int getDestinationID() {
        return this.destinationID;
    }

    public RegistrySendsNodeManifest(ArrayList<RoutingEntry> list, int[] listOfNodeIDs){
        routes = new ArrayList<RouteInfo>();
        this.numberOfNodeIDs = (byte)listOfNodeIDs.length;
        this.listOfNodeIDs = listOfNodeIDs;
        this.destinationID = list.get(0).getID();
        for (RoutingEntry re : list){
            RouteInfo rInfo = new RouteInfo();
            rInfo.IPAddress = re.getSocket().getInetAddress().getAddress();
            rInfo.IPLength = (byte)rInfo.IPAddress.length;
            rInfo.nodeID = re.getID();
            rInfo.port = re.getSocket().getPort();
            routes.add(rInfo);
        }
    }
    public int getNodeID(int index){
        return routes.get(index).nodeID;
    }

    public int getPort(int index){
        return routes.get(index).port;
    }

    public byte[] getIP(int index){
        return routes.get(index).IPAddress;
    }

    public byte getRoutingTableSize() {
        return routingTableSize;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeByte(routingTableSize);
        for (RouteInfo rI : routes){
            dout.writeInt(rI.nodeID);
            dout.writeByte(rI.IPLength);
            dout.write(rI.IPAddress);
            dout.writeInt(rI.port);
        }
        dout.writeByte(numberOfNodeIDs);
        for (int i = 0; i < numberOfNodeIDs; i++){
            dout.writeInt(listOfNodeIDs[i]);
        }
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public RegistrySendsNodeManifest(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        routingTableSize = din.readByte();
        routes = new ArrayList<RouteInfo>();
        for (int i = 0; i < routingTableSize; i++){
            RouteInfo rI = new RouteInfo();
            rI.nodeID = din.readInt();
            rI.IPLength = din.readByte();
            rI.IPAddress = new byte[rI.IPLength];
            din.readFully(rI.IPAddress);
            rI.port = din.readInt();
            routes.add(rI);
        }
        numberOfNodeIDs = din.readByte();
        listOfNodeIDs = new int[numberOfNodeIDs];
        for (int i = 0; i < numberOfNodeIDs; i++){
            listOfNodeIDs[i] = din.readInt();
        }
        baInputStream.close();
        din.close();
    }
}

class RouteInfo{
    int nodeID;
    byte IPLength;
    byte[] IPAddress;
    int port;
}
