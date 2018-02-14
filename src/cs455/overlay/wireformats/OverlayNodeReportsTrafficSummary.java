package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReportsTrafficSummary implements Event{
    private byte msgType = Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY;
    private int nodeID;
    private int packetsSent;
    private int packetsRelayed;
    private long sumOfPacketData;
    private int numberOfPacketsReceived;
    private long sumOfPacketsReceived;

    public byte getType(){
        return msgType;
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public int getPacketsRelayed() {
        return packetsRelayed;
    }

    public long getSumOfPacketData() {
        return sumOfPacketData;
    }

    public int getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

    public long getSumOfPacketsReceived() {
        return sumOfPacketsReceived;
    }



    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeInt(nodeID);
        dout.writeInt(packetsSent);
        dout.writeInt(packetsRelayed);
        dout.writeLong(sumOfPacketData);
        dout.writeInt(numberOfPacketsReceived);
        dout.writeLong(sumOfPacketsReceived);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public OverlayNodeReportsTrafficSummary(int nodeID, int packetsSent, int packetsRelayed,
                                            long sumOfPacketData, int numberOfPacketsReceived,
                                            long sumOfPacketsReceived){
        this.nodeID = nodeID;
        this.packetsSent = packetsSent;
        this.packetsRelayed = packetsRelayed;
        this.sumOfPacketData = sumOfPacketData;
        this.numberOfPacketsReceived = numberOfPacketsReceived;
        this. sumOfPacketsReceived = sumOfPacketsReceived;
    }

    public OverlayNodeReportsTrafficSummary(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        nodeID = din.readInt();
        packetsSent = din.readInt();
        packetsRelayed = din.readInt();
        sumOfPacketData = din.readLong();
        numberOfPacketsReceived = din.readInt();
        sumOfPacketsReceived = din.readLong();
        baInputStream.close();
        din.close();
    }
}
