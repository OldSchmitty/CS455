package cs455.scaling.server;

import java.util.LinkedList;

public class ConcurrentTaskList {
    private LinkedList<Task> taskList = new LinkedList<Task>();
    private boolean running = true;

    public synchronized void addTask(Task task){
        taskList.add(task);
        notify();
    }

    public void stop(){
        running = false;
    }

    public synchronized Task getTask(){
        while (taskList.size() <= 0 && running) {
            try {
                wait();
            }catch (java.lang.InterruptedException e){
                System.out.println(e);
            }
            if (taskList.size() <= 0 && running){
                return taskList.removeFirst();
            }
        }
        return null;
    }
}
