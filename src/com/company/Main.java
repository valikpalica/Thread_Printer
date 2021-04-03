package com.company;

import java.rmi.server.ServerCloneException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Service service = new Service();
        Printer printer1 = new Printer(service);
        Printer printer2 = new Printer(service);
        Client client1 = new Client(service,"client1");
        Client client2 = new Client(service,"client2");
        Client client3 = new Client(service,"client3");
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
class Service{
    private List<Client> clientList = new ArrayList<>();
    synchronized public void pull_client (Client client) throws InterruptedException {
        while (clientList.size()>=5){
            wait();
        }
        clientList.add(client);
        System.out.println(client.getName() + " add in query");
        notifyAll();
    }
    synchronized public void push_printer() throws InterruptedException {
        while (clientList.size()<1){
            wait();
        }
        Client client  = clientList.remove(0);
        System.out.println(client.getName()+" printed " + Thread.currentThread().getName());
        notifyAll();
    }
}
class Client implements Runnable{
    private Service _service;
    private String name ;
    Client(Service service, String name){
        _service = service;
        this.name = name;
    }
    public String getName () {
        return this.name;
    }
    @Override
    public void run() {
        try {
            for (int i=0;i<10;i++) {
                TimeUnit.SECONDS.sleep(1);
                _service.pull_client(this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
