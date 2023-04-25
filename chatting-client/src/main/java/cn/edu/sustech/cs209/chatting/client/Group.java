package cn.edu.sustech.cs209.chatting.client;

import java.util.List;

public class Group {
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
