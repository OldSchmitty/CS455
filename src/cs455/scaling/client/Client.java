package cs455.scaling.client;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.Random;

public class Client {
    private static final int msgSize = 8000;
    private LinkedList<String> hashList = new LinkedList<String>();
    private Selector selector;
    private InetAddress hostAddress;
    private int port;
    private int messageRate;
    private ClientReceiverThread receiver;
    private ClientStatisticsThread stats;

    public Client(String hostAddress, int port, int messageRate){
        try {
            this.hostAddress = InetAddress.getByName(hostAddress);
        }catch (java.net.UnknownHostException e){
            System.out.println(e);
            System.exit(1);
        }
        try{
            selector = Selector.open();
        }catch (java.io.IOException e){
            System.out.println(e);
            System.exit(1);
        }
        this.port = port;
        this.messageRate = messageRate;
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
        byte[] bytes = new byte[msgSize];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public synchronized void sendMessage(SocketChannel channel) {
        byte[] randomBytes = makeRandomBytes();
        String SHA1 = SHA1FromBytes(randomBytes);
        addHash(SHA1);
        ByteBuffer buffer = ByteBuffer.wrap(randomBytes);
        try {
            channel.write(buffer);
        }catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    private void startClient() throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(this.hostAddress, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        stats = new ClientStatisticsThread();
        stats.start();
        receiver = new ClientReceiverThread(selector,hashList, stats);
        receiver.start();
        boolean test = true;
        while(true) {
            if (channel.isConnected()) {
                sendMessage(channel);
                stats.incrMessagedSent();
            }
            try {
                Thread.sleep(1000/messageRate);
            }catch (java.lang.InterruptedException e){
                System.out.println(e);
            }
        }
    }

    public static void main(String args[]){
        if(args.length == 3) {
            Client client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            try {
                client.startClient();
            } catch (java.io.IOException e) {
                System.out.println(e);
            }
        }
        else{
            System.out.println("Error: Must provide 3 arguments, server-host, server-port, and message-rate");
        }
    }

}