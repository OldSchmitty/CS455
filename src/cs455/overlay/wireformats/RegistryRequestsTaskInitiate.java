package cs455.overlay.wireformats;

import java.io.*;

public class RegistryRequestsTaskInitiate implements Event{
    private byte msgType = Protocol.REGISTRY_REQUESTS_TASK_INITIATE;

    int numberOfPackets;

    public RegistryRequestsTaskInitiate(int num){
        this.numberOfPackets = num;
    }

    public byte getType(){
        return msgType;
    }
    public int getNumberOfPackets(){
        return numberOfPackets;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeInt(numberOfPackets);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public RegistryRequestsTaskInitiate(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        numberOfPackets = din.readInt();
        baInputStream.close();
        din.close();
    }
}
