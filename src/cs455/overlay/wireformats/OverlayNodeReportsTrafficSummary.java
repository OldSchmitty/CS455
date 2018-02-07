package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReportsTrafficSummary implements Event{
    private byte msgType;

    public byte getType(){
        return msgType;
    }
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
