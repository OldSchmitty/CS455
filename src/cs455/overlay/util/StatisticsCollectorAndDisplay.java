package cs455.overlay.util;
import java.util.TreeMap;



public class StatisticsCollectorAndDisplay {

    String[] Statistics = {"Key","Packets Sent", "Packets Recieved", "Packets Relayed", "Sum Values Sent", "Sum Values Received"};
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
        total = 0;
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
            System.out.printf(String.format("%25s",Statistics[i]));
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

            System.out.println(String.format("%25d", key)+String.format("%25d", stats.messagesSent)+String.format("%25d", stats.messagesReceived)
                    +String.format("%25d",stats.messagesRelayed)+String.format("%25d", stats.sumOfSent)+String.format("%25d", stats.sumOfRecieved));

        }
        System.out.println();
        System.out.println(String.format("%25s", "Total")+String.format("%25d",sentTotal)+String.format("%25d", receivedTotal)+String.format("%25d",relayedTotal)+
                String.format("%25d",sumSumOfSent)+String.format("%25d",sumSumofReceived));
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

