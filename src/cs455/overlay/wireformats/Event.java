package cs455.overlay.wireformats;

public interface Event {
    public byte getType();
    public byte[] getBytes() throws java.io.IOException;
}
