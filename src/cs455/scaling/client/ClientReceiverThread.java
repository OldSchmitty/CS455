package cs455.scaling.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class ClientReceiverThread extends Thread {
    private Selector selector;
    private LinkedList<String> hashList;
    private ClientStatisticsThread stats;


    public ClientReceiverThread(Selector selector, LinkedList<String> hashList, ClientStatisticsThread stats){
        this.selector = selector;
        this.hashList = hashList;
        this.stats = stats;
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
        ByteBuffer buffer = ByteBuffer.allocate(80);
        int read = 0;
        try {
            read = channel.read(buffer);
            String hash = new String(buffer.array()).trim();
            synchronized (hashList){
                if (hashList.contains(hash)){
                    hashList.remove(hash);
                    stats.incrMessagedsReceived();
                }
                else if (read == -1) {
                    /* Connection was terminated by the client. */
                    key.cancel();
                    channel.close();
                }
                else{

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
