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
        dout.write(IPAddress,0,IPLength);
        dout.writeInt(portNum);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public byte[] getIPAddress() {
        return IPAddress;
    }

    public int getPortNum() {
        return portNum;
    }

    public OverlayNodeSendsRegistration(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        if (msgType == Protocol.OVERLAY_NODE_SENDS_REGISTRATION) {
            IPLength = din.readByte();
            IPAddress = new byte[IPLength];
            din.readFully(IPAddress);
            portNum = din.readInt();
        }
        else{
            msgType = -1;
        }
        baInputStream.close();
        din.close();
    }

    public OverlayNodeSendsRegistration(byte[] IPAddress, int portNum){
        this.msgType = Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
        this.IPAddress = IPAddress;
        this.IPLength = (byte)this.IPAddress.length;
        this.portNum = portNum;
    }
}