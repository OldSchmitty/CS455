package cs455.scaling.cs455.scaling.client;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.LinkedList;

public class Client {
    private LinkedList<String> hashList = new LinkedList<String>();
    private Selector selector;
    private InetAddress hostAddress;
    private int port;

    public Client(String hostAddress, int port){
        try {
            this.hostAddress = InetAddress.getByName(hostAddress);
        }catch (java.net.UnknownHostException e){
            System.out.println(e);
        }
        this.port = port;
    }

    public synchronized boolean checkHash(String hash) {
        if (hashList.contains(hash)) {
            hashList.remove(hash);
            return true;
        } else {
            return false;
        }
    }

    private void addHash(String hash) {
        hashList.add(hash);
    }

    private String SHA1FromBytes(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);
            return hashInt.toString(16);
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return "";
    }

    private byte[] makeRandomBytes() {

    }

    public synchronized void sendMessage() {
        byte[] randomBytes = makeRandomBytes();
        String SHA1 = SHA1FromBytes(randomBytes);
        addHash(SHA1);
        //SEND OVER WIRE HERE
    }

    private void startClient() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(this.hostAddress, this.port));
        while (true) {
            for (SelectionKey key : selector.keys()){
                if (key.isConnectable()) {
                    this.connect(key);
                }
            }
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }
}