package cs455.overlay.transport;

import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;

public class TCPConnection {
    private TCPSenderThread sender;
    private TCPReceiverThread receiver;
    public TCPConnection(Socket socket, Node node) {
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

    public void close(){
        sender.close();
        receiver.close();
    }

    public void sendMessage(Event event){
        try {
            sender.addToQueue(event.getBytes());
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }
}
