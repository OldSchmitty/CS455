package cs455.overlay.transport;

import cs455.overlay.wireformats.Event;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


public class TCPSenderThread extends Thread
{
    private Socket socket;
    private DataOutputStream dout;
    private ArrayList<byte[]> queue;
    private volatile boolean open;

    public TCPSenderThread(Socket socket) throws IOException {
        this.socket = socket;
        dout = new DataOutputStream(socket.getOutputStream());
        open = true;
        queue = new ArrayList<>();
    }

    public synchronized void close(){
        open = false;
        notify();
    }

    public synchronized void addToQueue(byte[] data){
        queue.add(data);
        notify();
    }

    private synchronized void sendData(byte[] dataToSend) throws IOException
    {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }

    public synchronized void checkQueue(){
        while(queue.size() < 1 && open) {
            try {
                wait();
            } catch (java.lang.InterruptedException e) {
                System.out.println(e);
            }
            while (queue.size() > 0) {
                try {
                    sendData(queue.get(0));
                    queue.remove(0);
                }catch (java.io.IOException e){
                    System.out.println(e);
                }
            }
        }

    }

    @Override
    public void run() {
        checkQueue();
    }
}