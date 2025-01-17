package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private int serverPort;

    public Main(int serverPort) {
        this.serverPort = serverPort;
        this.excute();
    }
    public void excute() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            resetText("聊天室小程序已启动");
            while (true) {
                Socket socket = serverSocket.accept();
                resetText("有新的朋友加入");
                executorService.execute(new SocketHandler(socket));
            }
        } catch (IOException e) {
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
    public static void resetText(String info) {
        String jta = info + "\n";
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        Main server = new Main(1111);
    }
}






