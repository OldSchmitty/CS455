package cs455.scaling.server;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class ThreadPoolManager {
    private WorkerThread[] threads;
    private ConcurrentTaskList taskList = new ConcurrentTaskList();
    private ServerStatistics stats = new ServerStatistics();
    private ServerOutputThread output = new ServerOutputThread(stats);

    public void addConnection(SocketChannel channel){
        stats.addConnection(channel);
    }


    public void addTask(SelectionKey key){
        Task task = new Task(key);
        taskList.addTask(task);
    }

    public ThreadPoolManager(int numOfThreads){
        threads = new WorkerThread[numOfThreads];
    }

    public void startThreads() {
        final CountDownLatch startGate = new CountDownLatch(1);

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new WorkerThread(startGate, taskList, stats);
            threads[i].start();
        }
        startGate.countDown();
        output.start();
    }

}
