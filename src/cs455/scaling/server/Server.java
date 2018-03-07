package cs455.scaling.server;

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
    private Selector selector;
    private int portNum;
    private int threadPoolSize;
    private ThreadPoolManager manager;

    public Server(int portNum, int threadPoolSize){
        this.portNum = portNum;
        this.threadPoolSize = threadPoolSize;
        try {
            selector = Selector.open();
        }catch (java.io.IOException e){
            System.out.println(e);
            System.exit(1);
        }
        manager = new ThreadPoolManager(threadPoolSize);
    }

    private void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        manager.startThreads();
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) { // Accept client connections
                    SocketChannel channel = this.accept(key);
                    manager.addConnection(channel);
                    keys.remove();
                } else if (key.isReadable()) { // Read from client
                    key.interestOps(SelectionKey.OP_WRITE);
                    manager.addTask(key);
                }
            }
        }
    }

    private SocketChannel accept(SelectionKey key) throws IOException {
        ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = servSocket.accept();
        System.out.println("Accepting incoming connection ");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        return channel;
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

