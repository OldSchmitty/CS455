package cs455.scaling.cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {

    private Selector selector;
    private void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetAddres(...));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            this.selector.select();
            for(SelectionKey key : this.selector.selectedKeys()){
                if (key.isAcceptable()) {
                    this.accept(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = servSocket.accept();
        System.out.println("Accepting incoming connection ");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key, int buffSize) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConcurrentByteBuffer buffer = new ConcurrentByteBuffer(buffSize);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                synchronized (buffer) {
                    read = channel.read(buffer.getBuffer());
                }
            }
        } catch (IOException e) {
            // Cancel the key and close the socket channel
            key.cancel();
            channel.close();
            System.out.println(e);
        }

        if (read == -1) {
        /* Connection was terminated by the client. */
        key.cancel();
        channel.close();
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConcurrentByteBuffer buffer = new ConcurrentByteBuffer(data.length);
        synchronized (buffer) {
            channel.write(buffer.getBuffer());
        }
        key.interestOps(SelectionKey.OP_READ);
    }
}

