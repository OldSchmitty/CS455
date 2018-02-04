package cs455.overlay.transport;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.node.Node;


public class TCPReceiverThread extends Thread{
    private Socket socket;
    private DataInputStream din;
    public TCPReceiverThread(Socket socket, Node node) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                dataLength = din.readInt();
                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
                EventFactory.parse(data);
            } catch (SocketException se) {
                System.out.println(se.getMessage());
                break;
            }  catch (IOException ioe) {
                System.out.println(ioe.getMessage()) ;
                break;
            }
        }
    }
}