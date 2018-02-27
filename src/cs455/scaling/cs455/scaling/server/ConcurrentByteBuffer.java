package cs455.scaling.cs455.scaling.server;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ConcurrentByteBuffer {

    private ByteBuffer byteBuffer;

    public ConcurrentByteBuffer(int size){
        byteBuffer = ByteBuffer.allocate(size);
    }

    //Reads from bugger using FIFO method
    synchronized public byte readFifo() {
        byte rVal = (byte)0;
        while (byteBuffer.position() == 0){
            try {
                wait();
            }catch (InterruptedException e) {
                System.err.println("InterruptedException: " + e.getMessage());
            }
        }
        byteBuffer.flip();
        try{
            rVal=byteBuffer.get();
        } catch (BufferUnderflowException e) {
            System.err.println("InterruptedException: " + e.getMessage());
        }
        finally {
            byteBuffer.compact();
        }
        notify();
        return rVal;
    }




    //Writes to buffer
    synchronized public void write(byte b)
    {
        while(byteBuffer.position() >= byteBuffer.limit()){
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("InterruptedException: " + e.getMessage());
            }
        }
        try {
            byteBuffer.put(b);
        } catch(BufferOverflowException e) {
            System.err.println("BufferOverflowException: " + e.getMessage());
        }
        notify();
    }

    public synchronized boolean hasRemaining(){
        return byteBuffer.hasRemaining();
    }

    public synchronized ByteBuffer getBuffer(){
        return byteBuffer;
    }
}