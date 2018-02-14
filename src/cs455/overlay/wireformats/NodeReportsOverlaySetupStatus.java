package cs455.overlay.wireformats;

import java.io.*;

public class NodeReportsOverlaySetupStatus implements Event{
    private byte msgType = Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS;
    private int successStatus;
    private byte lengthOfInfo;
    private String informationString;

    public byte getType(){
        return msgType;
    }
    public int getSuccessStatus(){
        return successStatus;
    }
    public String getInformationString(){
        return informationString;
    }

    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeByte(msgType);
        dout.writeInt(successStatus);
        byte[] byteString = informationString.getBytes();
        lengthOfInfo = (byte)byteString.length;
        dout.writeByte(lengthOfInfo);
        dout.write(byteString);
        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshaledBytes;
    }

    public NodeReportsOverlaySetupStatus(int successStatus, String error){
        this.successStatus = successStatus;
        if (successStatus != -1){
            informationString = "Overlay Setup was a Success for node "+successStatus+".";
        }
        else{
            informationString = error;
        }
    }

    public NodeReportsOverlaySetupStatus(byte[] marshaledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshaledBytes);
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        msgType = din.readByte();
        successStatus = din.readInt();
        lengthOfInfo = din.readByte();
        byte[] byteString = new byte[lengthOfInfo];
        din.readFully(byteString);
        informationString = new String(byteString);
        baInputStream.close();
        din.close();
    }
}
