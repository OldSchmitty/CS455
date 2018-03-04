package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class ServerStatistics {
    private HashMap<SocketChannel, Integer> map = new HashMap<SocketChannel, Integer>();

    public int[] getStats(){
        int i = 0;
        int[] statsArray;
        synchronized (this){
            statsArray = new int[map.size()];
            for (int value : map.values()){
                statsArray[i] = value;
                i++;
            }
            return statsArray;
        }
    }

    public void addConnection(SocketChannel channel){
        synchronized (this) {
            map.put(channel, 0);
        }
    }

    public void increaseMsgSent(SocketChannel channel){
        synchronized (this){
            map.put(channel, map.get(channel)+1);
        }
    }

}
