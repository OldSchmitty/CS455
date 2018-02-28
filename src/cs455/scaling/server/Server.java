package cs455.scaling.server;

import cs455.scaling.tools.ConcurrentByteBuffer;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.Iterator;

public class Server {
    private static final int bufferSize = 8000;
    private Selector selector;
    private int portNum;
    private int threadPoolSize;

    public Server(int portNum, int threadPoolSize){
        this.portNum = portNum;
        this.threadPoolSize = threadPoolSize;
        try {
            selector = Selector.open();
        }catch (java.io.IOException e){
            System.out.println(e);
            System.exit(1);
        }
    }

    private void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress("localhost", portNum));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) { // Accept client connections
                    this.accept(key);
                    keys.remove();
                } else if (key.isReadable()) { // Read from client
                    this.read(key);
                } else if (key.isWritable()) {
                    // write data to client...
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

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                synchronized (buffer) {
                    read = channel.read(buffer);
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
        //key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConcurrentByteBuffer buffer = new ConcurrentByteBuffer(data.length);
        synchronized (buffer) {
            channel.write(buffer.getBuffer());
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    public String SHA1FromBytes(byte  [] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);
            return hashInt.toString(16);

        }catch (java.security.NoSuchAlgorithmException e){
            System.out.println(e);
            return "";
        }

    }

    public static void main(String args[]) {
        if (args.length == 2) {
            Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            try {
                server.startServer();
            }catch(java.io.IOException e){
                System.out.println(e);
            }

        }
        else{
            System.out.println("Error: Must provide 2 arguments, port and thread pool size");
        }
    }

}

