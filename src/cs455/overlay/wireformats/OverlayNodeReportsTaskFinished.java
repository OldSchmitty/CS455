package cs455.overlay.wireformats;

import javax.swing.text.html.ObjectView;
import java.io.*;

public class OverlayNodeReportsTaskFinished implements Event{
    private byte msgType = Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED;
    byte lengthOIP;
    byte[] IPAddress;
    int portNumber;
    int nodeID;

    public byte getType(){
        return msgType;
    }

    public byte[] getIPAddress() {
        return IPAddress;
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeByte(lengthOIP);
        dout.write(IPAddress);
        dout.writeInt(portNumber);
        dout.writeInt(nodeID);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public OverlayNodeReportsTaskFinished(byte[] IPAddress, int portNumber, int nodeID){
        this.IPAddress = IPAddress;
        this.lengthOIP = (byte)IPAddress.length;
        this.portNumber = portNumber;
        this.nodeID = nodeID;
    }

    public OverlayNodeReportsTaskFinished(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        lengthOIP = din.readByte();
        IPAddress = new byte[lengthOIP];
        din.readFully(IPAddress);
        portNumber = din.readInt();
        nodeID = din.readInt();
        baInputStream.close();
        din.close();
    }
}
