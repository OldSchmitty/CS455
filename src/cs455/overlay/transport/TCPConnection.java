package cs455.overlay.transport;

import java.net.Socket;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;

public class TCPConnection {
    private TCPSenderThread sender;
    private TCPReceiverThread receiver;
    private Socket socket;
    private int port;
    public TCPConnection(Socket socket, Node node, int port) {
        try {
            this.socket = socket;
            this.port = port;
            sender = new TCPSenderThread(this.socket);
        }catch (java.io.IOException e){
            System.out.println(e);
        }
        try {
            receiver = new TCPReceiverThread(this.socket, node);
        }catch (java.io.IOException e){
            System.out.println(e);
        }

        sender.start();
        receiver.start();
    }

    public Socket getSocket() {
        return socket;
    }
    public int getPort(){
        return port;
    }

    public void close(){
        sender.close();
        try {
            socket.close();
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    public void sendMessage(Event event){
        try {
            sender.addToQueue(event.getBytes());
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }
}
