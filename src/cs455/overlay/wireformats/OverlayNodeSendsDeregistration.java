package cs455.overlay.wireformats;

import sun.security.x509.IPAddressName;

import java.io.*;

public class OverlayNodeSendsDeregistration implements Event{
    private byte msgType = Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION;
    private byte IPAddressLength;
    private byte[] IPAddress;
    private int portNumber;
    private int nodeID;

    public byte getType() {
        return msgType;
    }

    public int getNodeID(){
        return this.nodeID;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeByte(IPAddressLength);
        dout.write(IPAddress);
        dout.writeInt(portNumber);
        dout.writeInt(nodeID);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public OverlayNodeSendsDeregistration(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        IPAddressLength = din.readByte();
        IPAddress = new byte[IPAddressLength];
        din.readFully(IPAddress);
        portNumber = din.readInt();
        nodeID = din.readInt();
        baInputStream.close();
        din.close();
    }

    public OverlayNodeSendsDeregistration(byte[] IPAddress, int portNum, int nodeID){
        this.IPAddress = IPAddress;
        this.IPAddressLength = (byte)IPAddress.length;
        this.portNumber = portNum;
        this.nodeID = nodeID;
    }
}
