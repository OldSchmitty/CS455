package cs455.scaling.server;
import java.sql.Time;
import java.sql.Timestamp;

public class ServerOutputThread extends Thread {
    private ServerStatistics stats;

    public ServerOutputThread(ServerStatistics stats){
        this.stats = stats;
    }

    public void printStats(){
        int totalMsgs = 0;
        double meanMsgs;
        double stdDev = 0;

        int[] statsArray = stats.getStats();
        for (int i = 0; i<statsArray.length; i ++){
            totalMsgs += statsArray[i];
        }
        if (totalMsgs != 0 && statsArray.length != 0) {
            meanMsgs = totalMsgs / statsArray.length;
        }
        else{
            meanMsgs = 0;
        }
        for (int i = 0; i < statsArray.length; i++)
        {
            stdDev += Math.pow((statsArray[i] - meanMsgs),2);
        }
        stdDev = stdDev/(statsArray.length-1);
        stdDev = Math.sqrt(stdDev);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        System.out.println("["+timestamp+"]"+"Server Throughput: "+totalMsgs+"message/s, Active Client Connections: "+
                statsArray.length+", Mean Per-client Throughput: "+meanMsgs+" message/s, Std. Dev. Of Per-client"
                +"Throughput: "+stdDev+" message/s");
        totalMsgs = 0;
    }

    public void run(){
        while (true){
            try {
                Thread.sleep(20000);
                printStats();

            }catch(java.lang.InterruptedException e){
                System.out.println(e);
            }

        }
    }

}
