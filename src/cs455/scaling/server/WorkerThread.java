package cs455.scaling.server;

import java.util.concurrent.CountDownLatch;

public class WorkerThread extends Thread {
    private CountDownLatch startGate;
    private ConcurrentTaskList taskList;
    private ServerStatistics stats;

    WorkerThread(CountDownLatch startGate, ConcurrentTaskList taskList, ServerStatistics stats){
        this.startGate = startGate;
        this.taskList = taskList;
        this.stats = stats;
    }

    public void run(){
        try {
            startGate.await();
            Task task = taskList.getTask();
            while (task != null){
                task.run();
                stats.increaseMsgSent(task.getChannel());
                task = taskList.getTask();
            }

        }catch (java.lang.InterruptedException e){
            System.out.println(e);
        }

    }
}
