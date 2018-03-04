package cs455.scaling.server;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;

public class Task {
    private static final int bufferSize = 8000;
    private String hash;
    private byte[] data;
    private SelectionKey key;

    public Task(SelectionKey key){
        this.key = key;
    }

    public SocketChannel getChannel(){
        return (SocketChannel) key.channel();
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                read = channel.read(buffer);
            }
            data = buffer.array();
            byte b1 = data[2001];

        } catch (IOException e) {
            // Cancel the key and close the socket channel
            key.cancel();
            channel.close();
            System.out.println(e);
            data = new byte[0];
        }

        if (read == -1) {
        /* Connection was terminated by the client. */
            key.cancel();
            channel.close();
            data = new byte[0];
        }
    }

    private String SHA1FromBytes(byte [] data) {
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

    private void write(byte[] data) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer = buffer.wrap(data);
        System.out.println("Wrote "+channel.write(buffer)+" bytes to "+channel.getRemoteAddress());
        key.interestOps(SelectionKey.OP_READ);
    }

    public void run(){
        try {
            read(key);
        }catch (java.io.IOException e){
            System.out.println(e);
        }
        if (data.length > 0) {
            hash = SHA1FromBytes(data);
            System.out.println("Got hash "+hash);
            try {
                write(hash.getBytes());
            }catch (java.io.IOException e){
                System.out.println(e);
            }
        }

    }

}
