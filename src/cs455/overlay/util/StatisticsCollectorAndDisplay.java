package cs455.overlay.util;
import java.util.TreeMap;



public class StatisticsCollectorAndDisplay {

    String[] Statistics = {"Packets Sent", "Packets Recieved", "Packets Relayed", "Sum Values Sent", "Sum Values Received"};
    TreeMap<Integer,GroupStats> map = new TreeMap<Integer, GroupStats>();

    public StatisticsCollectorAndDisplay(){}
    public void addItem(int id, int messagesSent, int messagesReceived, int messagesRelayed, long sumOfSent, long sumOfRecieved){
        GroupStats stats = new GroupStats();
        stats.messagesSent = messagesSent;
        stats.messagesReceived = messagesReceived;
        stats.messagesRelayed = messagesRelayed;
        stats.sumOfSent = sumOfSent;
        stats. sumOfRecieved = sumOfRecieved;
        map.put(id, stats);
    }

    public void reset(){
        map = new TreeMap<Integer, GroupStats>();
    }

    public void printStats(){
        for ( int i =0; i < Statistics.length; i++){
            System.out.print(Statistics[i]+"     ");
        }
        int sentTotal = 0;
        int receivedTotal = 0;
        int relayedTotal = 0;
        long sumSumOfSent = 0;
        long sumSumofReceived = 0;

        System.out.println();

        for (Integer key : map.keySet()){
            GroupStats stats = map.get(key);

            sentTotal += stats.messagesSent;
            receivedTotal += stats.messagesReceived;
            relayedTotal += stats.messagesRelayed;
            sumSumOfSent += stats.sumOfSent;
            sumSumofReceived += stats.sumOfRecieved;

            System.out.println(key+"     "+stats.messagesSent+"     "+stats.messagesReceived+"     "+
            stats.messagesRelayed+"     "+ stats.sumOfSent+"     "+ stats.sumOfRecieved);
            System.out.println();
            System.out.println(sentTotal+"     "+receivedTotal+"     "+relayedTotal+"     "+sumSumOfSent+"     "+sumSumofReceived);
        }

        System.out.println();
    }

}

class GroupStats{
    int messagesSent = 0;
    int messagesReceived = 0;
    int messagesRelayed = 0;
    long sumOfSent = 0;
    long sumOfRecieved = 0;
}

