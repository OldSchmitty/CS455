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
    private Node node;
    private boolean open;
    public TCPReceiverThread(Socket socket, Node node) throws IOException {
        this.socket = socket;
        this.node = node;
        din = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        int dataLength = 0;
        while (socket != null) {
            try {
                dataLength = din.readInt();
                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);
                EventFactory.parse(data, node);
            } catch (SocketException se) {
                //System.out.println(se.getMessage());
                break;
            } catch (IOException ioe) {
                if(dataLength != 0){
                    System.out.println(dataLength+" Socket at "+socket.getInetAddress().toString()+" on port "
                            +socket.getPort()+" unexpectedly closed.");
                }
                break;
            }
            dataLength = 0;
        }
    }
}