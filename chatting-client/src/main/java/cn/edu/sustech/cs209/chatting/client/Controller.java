package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;


import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    public TextArea inputArea;
  public Label currentUsername;
  public Label currentOnlineCnt;
    @FXML ListView<Message> chatContentList;

  static String username;

  private TCPClient tcpClient;
  private List<String> totalUsers=new ArrayList<>();
    private Thread readThread;
    private String nowSendTo;
    private boolean isGroup;
    private Group nowGroup;
    private String nowGN;

    @FXML
    ListView<String> chatList;

    List<Group> groupList = new ArrayList<>();
    List<Message> nowMLsit= new LinkedList<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            username = input.get();
            currentUsername.setText("Current user:"+username);
            try {
                tcpClient = new TCPClient("127.0.0.1", "8888");
                readThread = new Thread(()->{
                    String receiveMsg=null;
                    tcpClient.send("ask");
                    while ((receiveMsg=tcpClient.receive())!=null){
                        String msgTemp = receiveMsg;
                        Platform.runLater(()-> {
                            if (msgTemp.equals("clear")) {
                                totalUsers.clear();
                            } else if (msgTemp.contains("@")) {
                                String sendName = msgTemp.split("@")[0];
                                String reName = msgTemp.split("@")[0].split(":")[0];
                                String str = msgTemp.split("@")[1].split(":")[1];
                                System.out.println(sendName + str);

                                nowSendTo=sendName;
                                ObservableList<String> items=chatList.getItems();
                                if(!items.contains(nowSendTo)){
                                    items.add(nowSendTo);
                                }
                                chatList.refresh();
                                ObservableList<Message> me = chatContentList.getItems();
                                Message nowMsg = new Message(System.currentTimeMillis(), sendName, username, str);
                                nowMLsit.add(nowMsg);
                                chatContentList.setItems(me);
                                chatContentList.refresh();
                            }else if(msgTemp.contains("&")){
                                String sendName = msgTemp.split("&")[0];
                                String gName= msgTemp.split("&")[1].split(":")[0];
                                String str = msgTemp.split("&")[1].split(":")[1];
                                ObservableList<Message> me=chatContentList.getItems();
                                Message nowMsg=new Message(System.currentTimeMillis(),sendName,username,str,gName);
                                nowMLsit.add(nowMsg);
                                chatContentList.setItems(me);
                                chatContentList.refresh();
                                System.out.println(nowMsg.getSentBy());
                            }else if(msgTemp.contains("#")){
                                String[] temp= msgTemp.split("#")[1].split(",");
                                List<String> members = new ArrayList<>(Arrays.asList(temp));
                                String groupName = "";
                                for(int i=0;i<=2&&i<members.size();i++){
                                    groupName+=members.get(i)+",";
                                }
                                groupName=groupName.substring(0,groupName.length()-1);
                                if(members.size()>3){
                                    groupName+="...";
                                }
                                groupName+="("+members.size()+")";
                                Group group=new Group(groupName,members);

                                nowGroup =group;
                                ObservableList<String> items=chatList.getItems();
                                ObservableList<Message> chatsItems = chatContentList.getItems();
                                chatsItems.clear();
                                if(!items.contains(groupName)){
                                    items.add(groupName);
                                    String msg="Group:"+groupName+"-";
                                    for(String s:members){
                                        msg+=s+",";
                                    }
                                    msg=msg.substring(0,msg.length()-1);
                                    System.out.println(msg);
                                    tcpClient.send(msg);
                                }

                                for (Message c:nowMLsit){
                                    if (groupName.equals(c.getName())){
                                        chatsItems.add(c);
                                    }
                                }
                                chatList.setItems(items);
                                chatList.refresh();
                                chatContentList.setItems(chatsItems);
                                chatContentList.refresh();
                            } else{
                                totalUsers.add(msgTemp);
                            }
                            System.out.println(msgTemp+"\n");
                        });
                    }
                });
                readThread.start();
                if (totalUsers.contains(username)){
                    System.out.println("the username has already exit，please change!");
                }else{
                    tcpClient.send("Sign:"+username);
                    System.out.println("success");
                }

                 } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        chatContentList.setCellFactory(new MessageCellFactory());
        chatList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) ->{
                    String selectedItem = chatList.selectionModelProperty().get().getSelectedItem();
                    nowSendTo = selectedItem;
                    nowGN=selectedItem;
                    if(selectedItem.contains("(")){
                        isGroup=true;
                    }else {
                        isGroup=false;
                    }
                    ObservableList<Message> chatsItems = chatContentList.getItems();
                    chatsItems.clear();
                    for (Message c:nowMLsit){
                        if(nowGN.equals(c.getName())){
                                chatsItems.add(c);
                        }else if (nowSendTo.equals(c.getSentBy())||nowSendTo.equals(c.getSendTo())){
                            if(c.getName()==null){
                                chatsItems.add(c);
                            }
                        }
                    }
                    chatContentList.setItems(chatsItems);
                    chatContentList.refresh();
                });

    }


    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        tcpClient.send("ask");

        userSel.getItems().addAll(totalUsers);
        //user.set(userSel.getSelectionModel().getSelectedItem());

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        nowSendTo=user.get();
        System.out.println(nowSendTo);
        ObservableList<Message> me=chatContentList.getItems();
        ObservableList<String> items=chatList.getItems();
        me.clear();
        if(!items.contains(nowSendTo)){
            items.add(nowSendTo);
        }
        for (Message m:nowMLsit){
            if (user.get().equals(m.getSentBy())||user.get().equals(m.getSendTo())){
                me.add(m);
            }
        }
        chatList.setItems(items);
        chatList.refresh();
        chatContentList.setItems(me);
        chatContentList.refresh();
        isGroup=false;
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
       List<String> members=new ArrayList<>();

        Stage stage = new Stage();
        tcpClient.send("ask");

        List<CheckBox> checkBoxList=new ArrayList<>();
        for(String s:totalUsers){
            CheckBox c = new CheckBox(s);
            checkBoxList.add(c);
        }


        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            for(CheckBox c:checkBoxList){
                if(c.isSelected()){
                    members.add(c.getText());
                }
            }
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(checkBoxList);
        box.getChildren().addAll(okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        Collections.sort(members);
        if(!members.contains(username))members.add(username);
        String groupName = "";
        for(int i=0;i<=2&&i<members.size();i++){
            groupName+=members.get(i)+",";
        }
        groupName=groupName.substring(0,groupName.length()-1);
        if(members.size()>3){
            groupName+="...";
        }
        groupName+="("+members.size()+")";
        Group group=new Group(groupName,members);

        nowGroup =group;
        ObservableList<String> items=chatList.getItems();
        ObservableList<Message> chatsItems = chatContentList.getItems();
        chatsItems.clear();
        if(!items.contains(groupName)){
            items.add(groupName);
            String msg="Group:"+groupName+"-";
            for(String s:members){
                msg+=s+",";
            }
            msg=msg.substring(0,msg.length()-1);
            System.out.println(msg);
            tcpClient.send(msg);
        }

        for (Message c:nowMLsit){
            if (groupName.equals(c.getName())){
                chatsItems.add(c);
            }
        }
        chatList.setItems(items);
        chatList.refresh();
        chatContentList.setItems(chatsItems);
        chatContentList.refresh();
    }


    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
        String data = inputArea.getText();
        inputArea.clear();
        if (data.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Blank messages are not allowed!");
            alert.show();
            return;
        }

        if (isGroup){
            Message ms=new Message(System.currentTimeMillis(),username,nowGroup.getName(),data,nowGroup.getName());
            tcpClient.send(username+"&"+nowGroup.getName()+"-"+data);
            nowMLsit.add(ms);
            ObservableList<Message> Items = chatContentList.getItems();
            Items.add(ms);
            chatContentList.setItems(Items);
            chatContentList.refresh();;
        }else{
            sendToSever(nowSendTo,data);
        }

    }

    public void sendToSever(String sendTo,String data){
        Message ms=new Message(System.currentTimeMillis(),username,sendTo,data);
        tcpClient.send("@"+sendTo+"-"+data);
        nowMLsit.add(ms);
        ObservableList<Message> Items = chatContentList.getItems();
        Items.add(ms);
        chatContentList.setItems(Items);
        chatContentList.refresh();;
        System.out.println("test @"+sendTo+"-"+data);

    }

    public void doAsk(ActionEvent actionEvent) {
        tcpClient.send("ask");
        String ul=totalUsers.toString();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("online user:\n"+ul);
        alert.show();
    }


    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel;
                    if (msg.getSentBy().contains(":")) {
                        String s = msg.getSentBy().split(":")[1];
                        nameLabel = new Label(s);
                    }else{
                         nameLabel = new Label(msg.getSentBy());
                    }
                    Label msgLabel = new Label(msg.getData());
                    Label timeN=new Label(String.valueOf(msg.getTimestamp()));

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
