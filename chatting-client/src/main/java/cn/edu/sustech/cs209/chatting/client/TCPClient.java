package cn.edu.sustech.cs209.chatting.client;

import java.io.*;
import java.net.Socket;


public class TCPClient{
    private Socket socket= null;
    private PrintWriter pw;
    private BufferedReader br;

    public TCPClient(String ip, String port) throws IOException{
        socket =new Socket(ip,Integer.parseInt(port));

        OutputStream socketOut=socket.getOutputStream();
        pw=new PrintWriter(new OutputStreamWriter(socketOut,"utf-8"),true);

        InputStream socketIn=socket.getInputStream();
        br=new BufferedReader(new InputStreamReader(socketIn,"utf-8"));

    }


    public void send(String msg){
        pw.println(msg);
    }

    public String receive(){
        String msg=null;
        try {
            msg=br.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }

    public void close(){
        try {
            if (socket!=null)
                socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }



}
