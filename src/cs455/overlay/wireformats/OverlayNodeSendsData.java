package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeSendsData implements Event{
    private byte msgType = Protocol.OVERLAY_NODE_SENDS_DATA;
    private int destinationID;
    private int sourceID;
    private int payload;
    int disseminationTraceLength = 0;
    int[] disseminationTrace;

    public byte getType(){
        return msgType;
    }
    public void addTrace(int id){
        int[] newDissimination = new int[disseminationTrace.length+1];
        for(int i = 0; i < disseminationTrace.length; i++){
            newDissimination[i] = disseminationTrace[i];
        }
        newDissimination[newDissimination.length-1] = id;
        disseminationTraceLength++;
        disseminationTrace = newDissimination;
    }

    public int getDestinationID(){
        return destinationID;
    }

    public int getPayload(){
        return payload;
    }

    public int getDisseminationTraceLength() {
        return disseminationTraceLength;
    }

    public int[] getDisseminationTrace() {
        return disseminationTrace;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeInt(destinationID);
        dout.writeInt(sourceID);
        dout.writeInt(payload);
        dout.writeInt(disseminationTrace.length);
        for(int i = 0; i<disseminationTrace.length; i++){
            dout.writeInt(disseminationTrace[i]);
        }
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public OverlayNodeSendsData(int destinationID, int sourceID, int payload){
        this.destinationID = destinationID;
        this.sourceID = sourceID;
        this.payload = payload;
        this.disseminationTrace = new int[0];
    }

    public OverlayNodeSendsData(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        destinationID = din.readInt();
        sourceID = din.readInt();
        payload = din.readInt();
        disseminationTraceLength = din.readInt();
        disseminationTrace = new int[disseminationTraceLength];
        for(int i = 0; i<disseminationTrace.length; i++){
            disseminationTrace[i] = din.readInt();
        }
        baInputStream.close();
        din.close();
    }
}
