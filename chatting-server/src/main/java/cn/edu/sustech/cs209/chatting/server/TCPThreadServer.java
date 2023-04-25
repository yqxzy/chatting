//package cn.edu.sustech.cs209.chatting.server;
//
///*
// * TCPThreadServer.java
// * Copyright (c) 2020-11-02
// * author : Charzous
// * All right reserved.
// */
//
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class TCPThreadServer {
//    private int port =8008;//服务器监听窗口
//    private ServerSocket serverSocket;//定义服务器套接字
//    //创建动态线程池，适合小并发量，容易出现OutOfMemoryError
//    private ExecutorService executorService=Executors.newCachedThreadPool();
//
//    public TCPThreadServer() throws IOException{
//        serverSocket =new ServerSocket(8008);
//        System.out.println("服务器启动监听在"+port+"端口...");
//
//    }
//
//    private PrintWriter getWriter(Socket socket) throws IOException{
//        //获得输出流缓冲区的地址
//        OutputStream socketOut=socket.getOutputStream();
//        //网络流写出需要使用flush，这里在printWriter构造方法直接设置为自动flush
//        return new PrintWriter(new OutputStreamWriter(socketOut,"utf-8"),true);
//    }
//
//    private BufferedReader getReader(Socket socket) throws IOException{
//        //获得输入流缓冲区的地址
//        InputStream socketIn=socket.getInputStream();
//        return new BufferedReader(new InputStreamReader(socketIn,"utf-8"));
//    }
//
//    //多客户版本，可以同时与多用户建立通信连接
//    public void Service() throws IOException {
//        while (true){
//            Socket socket=null;
//            socket=serverSocket.accept();
//            //将服务器和客户端的通信交给线程池处理
//            Handler handler=new Handler(socket);
//            executorService.execute(handler);
//        }
//    }
//
//
//    class Handler implements Runnable {
//        private Socket socket;
//
//        public Handler(Socket socket) {
//            this.socket = socket;
//        }
//
//        public void run() {
//            //本地服务器控制台显示客户端连接的用户信息
//            System.out.println("New connection accept:" + socket.getInetAddress().getHostAddress());
//            try {
//                BufferedReader br = getReader(socket);
//                PrintWriter pw = getWriter(socket);
//
//                pw.println("From 服务器：欢迎使用服务！");
//                pw.println("请输入用户名：");
//                String localName = null;
//                while ((hostName=br.readLine())!=null){
//                    users.forEach((k,v)->{
//                        if (v.equals(hostName))
//                            flag=true;//线程修改了全局变量
//                    });
//
//                    if (!flag){
//                        localName=hostName;
//                        users.put(socket,hostName);
//                        flag=false;//可能找出不一致问题
//                        break;
//                    }
//                    else{
//                        flag=false;
//                        pw.println("该用户名已存在，请修改！");
//                    }
//                }
//
////                System.out.println(hostName+": "+socket);
//                sendToMembers("我已上线",localName,socket);
//                pw.println("输入命令功能：(1)L(list):查看当前上线用户;(2)G(group):进入群聊;(3)O(one-one):私信;(4)E(exit):退出当前聊天状态;(5)bye:离线;(6)H(help):帮助");
//
//                String msg = null;
//                //用户连接服务器上线，进入聊天选择状态
//                while ((msg = br.readLine()) != null) {
//                    if (msg.trim().equalsIgnoreCase("bye")) {
//                        pw.println("From 服务器：服务器已断开连接，结束服务！");
//
//                        users.remove(socket,localName);
//
//                        sendToMembers("我下线了",localName,socket);
//                        System.out.println("客户端离开。");//加当前用户名
//                        break;
//                    }
//                    else if (msg.trim().equalsIgnoreCase("H")){
//                        pw.println("输入命令功能：(1)L(list):查看当前上线用户;(2)G(group):进入群聊;(3)O(one-one):私信;(4)E(exit):退出当前聊天状态;(5)bye:离线;(6)H(help):帮助");
//                        continue;//返回循环
//                    }
//                    else if (msg.trim().equalsIgnoreCase("L")){
//                        users.forEach((k,v)->{
//                            pw.println("用户:"+v);
//                        });
//                        continue;
//                    }
//                    //一对一私聊
//                    else if (msg.trim().equalsIgnoreCase("O")){
//                        pw.println("请输入私信人的用户名：");
//                        String name=br.readLine();
//
//                        //查找map中匹配的socket，与之建立通信
//                        users.forEach((k, v)->{
//                            if (v.equals(name)) {
//                                isExist=true;//全局变量与线程修改问题
//                            }
//
//                        });
//                        //已修复用户不存在的处理逻辑
//                        Socket temp=null;
//                        for(Map.Entry<Socket,String> mapEntry : users.entrySet()){
//                            if(mapEntry.getValue().equals(name))
//                                temp = mapEntry.getKey();
//                        }
//                        if (isExist){
//                            isExist=false;
//                            //私信后有一方用户离开，另一方未知，仍然发信息而未收到回复，未处理这种情况
//                            while ((msg=br.readLine())!=null){
//                                if (!msg.equals("E")&&!isLeaved(temp))
//                                    sendToOne(msg,localName,temp);
//                                else if (isLeaved(temp)){
//                                    pw.println("对方已经离开，已断开连接！");
//                                    break;
//                                }
//                                else{
//                                    pw.println("您已退出私信模式！");
//                                    break;
//                                }
//                            }
//                        }
//                        else
//                            pw.println("用户不存在！");
//                    }
//                    //选择群聊
//                    else if (msg.trim().equals("G")){
//                        pw.println("您已进入群聊。");
//                        while ((msg=br.readLine())!=null){
//                            if (!msg.equals("E")&&users.size()!=1)
//                                sendToMembers(msg,localName,socket);
//                            else if (users.size()==1){
//                                pw.println("当前群聊无其他用户在线，已自动退出！");
//                                break;
//                            }
//                            else {
//                                pw.println("您已退出群组聊天室！");
//                                break;
//                            }
//                        }
//
//                    }
//                    else
//                        pw.println("请选择聊天状态！");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (socket != null)
//                        socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    public static void main(String[] args) throws IOException{
//        new TCPThreadServer().Service();
//    }
//
//}
//
//
//
