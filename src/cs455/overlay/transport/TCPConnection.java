package cs455.overlay.transport;

import java.net.Socket;
import cs455.overlay.node.Node;
public class TCPConnection {
    private TCPSenderThread sender;
    private TCPReceiverThread receiver;
    TCPConnection(Socket socket, Node node) {
        try {
            sender = new TCPSenderThread(socket);
        }catch (java.io.IOException e){
            System.out.println(e);
        }
        try {
            receiver = new TCPReceiverThread(socket, node);
        }catch (java.io.IOException e){
            System.out.println(e);
        }

        sender.start();
        receiver.start();
    }
}
