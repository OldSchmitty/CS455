package cs455.overlay.wireformats;


import java.io.*;
import java.net.InetAddress;



/*public class WireFormatWidget {
    private int type;
    private long timestamp;
    private String identifier;
    private int tracker;
}*/

public class OverlayNodeSendsRegistration {
    private byte msgType = Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
    private byte IPLength;
    private String IPAddress = null;        //InetAddress.getAddress();
    private int portNum;




    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeInt(msgType);
        dout.writeInt(IPLength);
        byte[] IPAddressBytes = IPAddress.getBytes();
        IPLength = (byte)IPAddressBytes.length;
        dout.writeInt(IPLength);
        dout.write(IPAddressBytes);
        dout.writeInt(portNum);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    public OverlayNodeSendsRegistration(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        IPLength = din.readByte();
        byte[] identifierBytes = new byte[IPLength];
        din.readFully(identifierBytes);
        IPAddress = new String(identifierBytes);
        portNum = din.readInt();
        baInputStream.close();
        din.close();
    }
}