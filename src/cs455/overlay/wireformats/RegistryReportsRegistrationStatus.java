package cs455.overlay.wireformats;

import com.sun.net.httpserver.Authenticator;
import cs455.overlay.node.Registry;

import java.io.*;

public class RegistryReportsRegistrationStatus implements Event{
    private byte msgType;
    private int sucessStatus;
    private byte informationLength;
    private String informationString;

    public byte getType(){
        return msgType;
    }

    public int getSucessStatus(){
        return sucessStatus;
    }

    public String getInformationString() {
        return informationString;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeInt(sucessStatus);
        byte infoLength = (byte)informationString.length();
        dout.writeByte(infoLength);
        dout.writeBytes(informationString);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public RegistryReportsRegistrationStatus(int sucessStatus, int overlayNum, String errorInfo){
        this.msgType = Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS;
        this.sucessStatus=sucessStatus;
        if (sucessStatus != -1){
            this.informationString = new String("Registration request successful. The number of " +
                    "messaging nodes currently constituting the overlay" +
                    " is ("+overlayNum+")");
        }
        else{
            this.informationString = errorInfo;
        }

        this.informationLength = (byte)informationString.length();
    }

    public RegistryReportsRegistrationStatus(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        sucessStatus = din.readInt();
        informationLength = din.readByte();
        byte[] byteString = new byte[informationLength];
        din.readFully(byteString);
        informationString = new String(byteString);
        baInputStream.close();
        din.close();
    }
}
