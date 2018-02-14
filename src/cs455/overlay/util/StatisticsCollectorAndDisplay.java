package cs455.overlay.util;
import java.util.TreeMap;



public class StatisticsCollectorAndDisplay {

    String[] Statistics = {"Packets Sent", "Packets Recieved", "Packets Relayed", "Sum Values Sent", "Sum Values Received"};
    TreeMap<Integer,GroupStats> map = new TreeMap<Integer, GroupStats>();
    private int total = 0;

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

    public int getTotal(){
        return total;
    }

    public void IncrementTotal() {
        total++;
    }

    public void printStats(){
        String line;
        for ( int i =0; i < Statistics.length; i++){
            System.out.printf(String.format("%15d",Statistics[i]));
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

            System.out.printf(String.format("%15d", key)+String.format("%15d", stats.messagesSent)+String.format("%15d", stats.messagesReceived)
                    +String.format("%15d",stats.messagesRelayed)+String.format("%15d", stats.sumOfSent)+String.format("%15d", stats.sumOfRecieved));

        }
        System.out.println();
        System.out.println(String.format("%15d", "Total")+String.format("%15d",sentTotal)+String.format("%15d", receivedTotal)+String.format("%15d",relayedTotal)+
                String.format("%15d",sumSumOfSent)+String.format("%15d",sumSumofReceived));
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

