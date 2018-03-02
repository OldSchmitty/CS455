package cs455.scaling.server;

public class ServerOutputThread extends Thread {
    private ServerStatistics stats;

    public ServerOutputThread(ServerStatistics stats){
        this.stats = stats;
    }

    public void printStats(){
        int totalMsgs = 0;
        double meanMsgs;
        double stdDev;

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
        System.out.println("Total: "+totalMsgs+" Mean: "+meanMsgs);


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
