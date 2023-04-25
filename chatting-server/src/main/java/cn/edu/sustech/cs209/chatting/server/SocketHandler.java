package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class SocketHandler implements Runnable{
    private static Map<String,Socket> map = new ConcurrentHashMap<String, Socket>();
    private Socket socket;
    private static List<Group> groups=new ArrayList<>();

    public SocketHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            String msg = null;
            PrintStream pr= new PrintStream(socket.getOutputStream());
           // pr.println("welcome to chatroom!");

            while(true){
                if(scanner.hasNextLine()) {
                    msg = scanner.nextLine();
                    if (msg.startsWith("Sign:")) {
                        String userName = msg.split(":")[1];
                        if (!map.containsKey(userName)) {
                            map.put(userName, socket);
                            System.out.println("[用户: " + userName + "] 上线了，他的[客户端为: " + socket + "]!");
                            System.out.println("当前在线人数为:" + map.size() + "人");
                        }
                        continue;
                    } else if (msg.contains("quit")) {
                        userExit(socket);
                        break;
                    } else if (msg.startsWith("@") && msg.contains("-")) { // @userName-私聊信息
                        String userName = msg.split("@")[1].split("-")[0];
                        String str = msg.split("@")[1].split("-")[1];
                        System.out.println(userName);
                        privateChat(socket, userName, str);
                        continue;
                    }else if(msg.equals("ask")){
                        pr.println("clear");
                        Set<Map.Entry<String,Socket>> set = map.entrySet();
                        for(Map.Entry<String,Socket> entry : set){
                            pr.println(entry.getKey());
                        }
                    }else if(msg.contains("&") && msg.contains("-")){
                        String sendp=msg.split("&")[0];
                        System.out.println(sendp);
                        String groupName = msg.split("&")[1].split("-")[0];
                        String str = msg.split("&")[1].split("-")[1];
                        groupChat(socket, groupName, str,sendp);
                        continue;
                    }else if(msg.startsWith("Group:")){//Group:name-a,b,c
                        System.out.println(msg);
                        String gName=msg.split(":")[1].split("-")[0];
                        List<String> members = Arrays.asList(msg.split("-")[1].split(","));
                        System.out.println(members.get(0));
                        System.out.println(gName+ " "+ members.size());
                        int p=0;
                        for(Group gr:groups){
                            if(gr.getName().equals(gName)){
                                p=1;
                                break;
                            }
                        }
                        if(p==0) {
                            Group g = new Group(gName, members);
                            groups.add(g);
                        }
                        groupSet(socket,gName);
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void groupChat(Socket socket,String groupN,String msg,String sendp) throws IOException {
        List<String> userNames =new ArrayList<>();
        for(Group gr:groups){
            if(gr.getName().equals(groupN)){
                userNames=gr.getMembers();
                break;
            }
        }
        for(int i=0;i<userNames.size();i++) {
                Socket nowSocket=map.get(userNames.get(i));
                if(!socket.equals(nowSocket)) {
                    PrintStream printStream = new PrintStream(nowSocket.getOutputStream());
                    printStream.println(sendp + "&" + groupN + ":" + msg);
                    System.out.println(groupN + "聊" + sendp + "说:" + msg);
                }
        }
    }

    private void privateChat(Socket socket, String userName, String msg) throws IOException {
        String curUser = null;
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        for(Map.Entry<String,Socket> entry : set){
            if(entry.getValue().equals(socket)){
                curUser=entry.getKey();
                break;
            }
        }
        Socket client = map.get(userName);
        PrintStream printStream = new PrintStream(client.getOutputStream());
        printStream.println(curUser+ "@"+userName+":"+ msg);
        System.out.println(curUser + "私聊" + userName + "说:" + msg);
    }

    private void groupSet(Socket socket, String groupN) throws IOException {
        List<String> userNames =new ArrayList<>();
        String temp="";
        for(Group gr:groups){
            if(gr.getName().equals(groupN)){
                userNames=gr.getMembers();
                break;
            }
        }
        for(String s:userNames){
            temp+=s+",";
        }
        temp=temp.substring(0,temp.length()-1);
        for(int i=0;i<userNames.size();i++) {
            Socket nowSocket=map.get(userNames.get(i));
            if(!socket.equals(nowSocket)) {
                PrintStream printStream = new PrintStream(nowSocket.getOutputStream());
                printStream.println("#" + temp);
                System.out.println(groupN + "mem" + temp);
            }
        }
    }

    private void userExit(Socket socket){
        String userName = null;
        for(String key:map.keySet()){
            if(map.get(key).equals(socket)){
                userName=key;
                break;
            }
        }
        map.remove(userName,socket);

        System.out.println("用户:"+ userName +"已下线!");
        System.out.println("当前在线人数为:" + map.size() + "人");
    }
}

class Group {
    private String name;
    private List<String> members;

    public Group(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getMembers() {
        return this.members;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

}
