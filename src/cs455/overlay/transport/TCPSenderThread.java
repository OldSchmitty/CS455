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
    private ArrayList<Event> queue;
    private Boolean open;

    public TCPSenderThread(Socket socket) throws IOException {
        this.socket = socket;
        dout = new DataOutputStream(socket.getOutputStream());
        open = true;
    }

    public void close(){
        open = false;
    }

    public void sendData(byte[] dataToSend) throws IOException
    {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }

    public synchronized void checkQueue(){
        if (queue.size() > 0){
            try {
                sendData(queue.get(0).getBytes());
            }catch(java.io.IOException e){
                System.out.println(e);
            }
        }
    }
    @Override
    public void run() {
        while(open){
            checkQueue();
        }
    }
}