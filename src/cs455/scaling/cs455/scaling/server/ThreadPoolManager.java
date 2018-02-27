package cs455.scaling.cs455.scaling.server;
import java.util.LinkedList;

public class ThreadPoolManager {
    private LinkedList<Task> taskList = new LinkedList<Task>();

    public synchronized void addTask(Task task){
        taskList.add(task);
    }

    public synchronized Task getTask(){
        return taskList.removeFirst();
    }
}
