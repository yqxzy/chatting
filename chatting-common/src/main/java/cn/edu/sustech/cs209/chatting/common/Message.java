package cn.edu.sustech.cs209.chatting.common;

public class Message {

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private String name;



    public Message(Long timestamp, String sentBy, String sendTo, String data,String name) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.name=name;
    }

    public Message(Long timestamp, String sentBy, String sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.name=null;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
    public String getName() {
        return name;
    }
}
