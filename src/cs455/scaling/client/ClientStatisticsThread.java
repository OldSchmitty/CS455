package cs455.scaling.client;
import java.sql.Timestamp;


public class ClientStatisticsThread extends Thread{
    private int messagesSent = 0;
    private int messagesReceived = 0;

    public synchronized void incrMessagedSent(){
        messagesSent++;
    }

    public synchronized void incrMessagedsReceived(){
        messagesReceived++;
    }

    private synchronized void printStats(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        System.out.println("["+timestamp+"] "+
                "Total Sent Count: "+messagesSent+", Total Received Count: "+messagesReceived);
        messagesSent = 0;
        messagesReceived = 0;
    }


    public void run(){
        while (true){
            try {
                Thread.sleep(20000);
            }
            catch(java.lang.InterruptedException e){
                System.out.println(e);
            }
            printStats();
        }

    }

}
