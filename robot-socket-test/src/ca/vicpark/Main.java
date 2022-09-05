package ca.vicpark;

import java.io.*;
import java.net.*;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Thread implements Closeable {
    private ServerSocket server = null;
    private Socket pi = null;
    private boolean isConnected = false;
    private int retryTimes = DEFAULT_RETRY_TIMES;
    private Scanner in = null;
    private long timeout;
    AtomicReference<String> value = new AtomicReference<String>();
    public static final int DEFAULT_RETRY_TIMES = 3;
    public static final int DEFAULT_TIMEOUT = 1200;
    public Main(boolean waitToConnect) throws IOException{
        super("The socket server daemon");
        this.server = new ServerSocket(44869);
        System.out.println("Daemon Started...");
        this.timeout = DEFAULT_TIMEOUT;
        if(waitToConnect) {
            this.isConnected = connect();
        }
        this.start();
    }

    public boolean isConnected(){
        return this.isConnected;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        if(retryTimes < 0){
            throw new InvalidParameterException(
                    "The retryTimes cannot be lower than zero");
        }
        this.retryTimes = retryTimes;
    }

    private boolean connect(){
        if(this.pi != null){
            System.err.println("Connection already exists.");
            return true;
        }
        int count = 0;
        do {
            try {
                System.out.println("Getting the connection from the pi...");
                this.pi = this.server.accept();
                System.out.println("Connection established.");
                this.in = new Scanner(pi.getInputStream());
                return true;
            } catch (IOException e) {
                count++;
                System.out.println(
                        "Failed to establish connection. " +
                                ((count<this.retryTimes)?"\nTry again.":""));
            }
        }while(count < this.retryTimes);
        return false;
    }

    public String getValue(){
        return this.value.get();
    }

    @Override
    public void run() {
        System.out.println("Threading started.");
        while (true) {
            if (this.pi == null) this.isConnected = connect();
            long initial_time = new Date().getTime();
            while (isConnected) {
                if (in.hasNextLine()) {
                    String tempValue = in.nextLine();
                    initial_time = new Date().getTime();
                    this.value.set(tempValue);
                    System.out.println(tempValue);
                }
                if (new Date().getTime() - initial_time > timeout) {
                    System.out.println("Connection stopped");
                    try{this.pi.close();}catch (IOException e){}
                    this.pi = null;
                    isConnected = false;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.interrupt();
    }

    public static void main(String[] args) throws IOException{
        new Main(true);
    }
}
