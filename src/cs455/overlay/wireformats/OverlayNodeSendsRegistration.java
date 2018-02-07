package cs455.overlay.wireformats;


import java.io.*;
import java.net.InetAddress;



/*public class WireFormatWidget {
    private int type;
    private long timestamp;
    private String identifier;
    private int tracker;
}*/

public class OverlayNodeSendsRegistration implements Event{
    private byte msgType = Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
    private byte IPLength;
    private byte[] IPAddress = null;
    private int portNum;

    public byte getType(){return msgType;}


    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeByte(IPLength);
        IPLength = (byte)IPAddress.length;
        dout.writeInt(IPLength);
        dout.write(IPAddress);
        dout.writeInt(portNum);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public OverlayNodeSendsRegistration(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        IPLength = din.readByte();
        byte[] identifierBytes = new byte[IPLength];
        din.readFully(identifierBytes);
        portNum = din.readInt();
        baInputStream.close();
        din.close();
    }
}