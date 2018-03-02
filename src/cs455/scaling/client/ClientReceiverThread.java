package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class ClientReceiverThread extends Thread {
    private Selector selector;
    private LinkedList<String> hashList;

    public ClientReceiverThread(Selector selector, LinkedList<String> hashList){
        this.selector = selector;
        this.hashList = hashList;
    }

    public void run(){
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    if (!key.isValid()) {
                        continue;
                    } else if (key.isReadable()) { // Read from client
                        read(key);
                    }
                }
            }catch(java.io.IOException e){
                System.out.println(e);
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8000);
        try {
            channel.read(buffer);
            String hash = new String(buffer.array()).trim();
            synchronized (hashList){
                if (hashList.contains(hash)){
                    hashList.remove(hash);
                }
                else{
                    System.out.println("Error: hashList does not contain hash "+hash);
                }
            }
        } catch (IOException e) {
            // Cancel the key and close the socket channel
            key.cancel();
            channel.close();
            System.out.println(e);
        }

    }
}
