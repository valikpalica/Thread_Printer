package com.company;
import java.rmi.server.ServerCloneException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int size_tasks = 10;
        List<String> arrayList = new ArrayList<>();
        for (int i=0;i<size_tasks;i++){
            arrayList.add("doc"+i);
        }
        Service service = new Service();
        Printer printer1 = new Printer(service);
        Printer printer2 = new Printer(service);
        Client client1 = new Client(service,"client1");
        client1.push_tasks((ArrayList<String>) arrayList,false);
        Client client2 = new Client(service,"client2");
        client2.push_tasks((ArrayList<String>) arrayList,true);
        Client client3 = new Client(service,"client3");
        client3.push_tasks((ArrayList<String>) arrayList,false);
        Thread thread_client_1 = new Thread(client1);
        Thread thread_client_2 = new Thread(client2);
        Thread thread_client_3 = new Thread(client3);
        Thread thread_printer_1 = new Thread(printer1);
        thread_printer_1.setName("printer 1");
        Thread thread_printer_2 = new Thread(printer2);
        thread_printer_2.setName("printer 2");
        Stream.of(thread_client_1,thread_client_2,thread_client_3,thread_printer_1,thread_printer_2).forEach(Thread::start);


    }
}
class Printer implements Runnable{
    private Service _service;
    Printer (Service service){
        _service = service;
    }
    @Override
    public void run() {
        try {
            while (true){
                TimeUnit.SECONDS.sleep(1);
                _service.push_printer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Task {
    private String task;
    private String name_owner;
    Task(String task,String name_owner){
        this.task = task;
        this.name_owner = name_owner;
    }
    public String getName(){
        return this.name_owner;
    }
    public String getTask(){
        return this.task;
    }
}


class Service{
    private List<Task> tasks = new ArrayList<>();
    synchronized public void pull_client (List<String> task,boolean prioritet,String name) throws InterruptedException {
        for (int i=0;i<task.size();i++){
            if(!prioritet&i%3==0){
                wait();
            }
            tasks.add(new Task(task.get(i),name));
        }
        notifyAll();
    }
    synchronized public void push_printer() throws InterruptedException {
        if(tasks.size()<=0){
            wait();
        }
        Task task = tasks.remove(0);
        notifyAll();
        System.out.println(task.getTask()+" printed "+ Thread.currentThread().getName()+" : owner "+task.getName());
    }
}
class Client implements Runnable{
    private Service service;
    private String name;
    private boolean prioritet;
    private List<String> listTasks;
    Client(Service service, String name){
        this.service = service;
        this.name = name;
    }
    public void push_tasks(ArrayList<String> task,boolean prioritet){
        this.prioritet = prioritet;
        listTasks = task;
    }
    @Override
    public void run() {
        try {
                TimeUnit.SECONDS.sleep(1);
                service.pull_client(listTasks,prioritet,name);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
