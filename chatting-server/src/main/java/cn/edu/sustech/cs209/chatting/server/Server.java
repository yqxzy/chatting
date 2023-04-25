package cn.edu.sustech.cs209.chatting.server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private int serverPort;

    public Server(int serverPort) {
        this.serverPort = serverPort;
        this.excute();
    }

    public void excute() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            ExecutorService executorService= Executors.newFixedThreadPool(20);
            System.out.println("Sever start:");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress()+"jion");
                executorService.execute(new SocketHandler(socket));
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args){
        Server server = new Server(8888);
    }
}


