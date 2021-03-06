package cs455.overlay.wireformats;
import java.io.*;

public class RegistryRequestsTrafficSummary implements Event{
    private byte msgType = Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY;

    public byte getType(){
        return msgType;
    }
    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public RegistryRequestsTrafficSummary(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        baInputStream.close();
        din.close();
    }
    public RegistryRequestsTrafficSummary(){}

}
