package com.example.messanger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;

public class HelloController {
    Client client;
    String user_to_send = "";
    String my_username;
    @FXML
    HBox connection_box;
    @FXML
    VBox chat;
    @FXML
    TextField username;
    @FXML
    TextField message;
    @FXML
    Button send;
    @FXML
    VBox messages;
    @FXML
    ListView<HBox> users;
    @FXML
    ScrollPane scrollPane;
    @FXML
    private HashMap<String, ArrayList<Pair<String, String>>> chats = new HashMap<>();
    @FXML
    public void connect() {
        client = new Client("localhost", 60000, this);
        if(username.getText().isEmpty() || !client.trySetUsername(username.getText())){
            client.close();
        }
        else {
            messages.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                    scrollPane.setVvalue(1.0);
                }
            });
            connection_box.setDisable(true);
            users.setDisable(false);
            my_username = username.getText();
            users.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            users.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(users.getSelectionModel().getSelectedItem() != null){
                        chat.setDisable(false);

                        TextFlow nameFlow = (TextFlow) users.getSelectionModel().getSelectedItem().getChildren().get(0);
                        Text name = (Text) nameFlow.getChildren().get(0);
                        user_to_send = name.getText();

                        Platform.runLater(() -> {
                            messages.getChildren().clear();
                            for(Pair<String, String> message: chats.get(user_to_send)){
                                HBox box = generateMessageBox(message.getValue());;
                                if(message.getKey().equals("out")){
                                    box.setAlignment(Pos.CENTER_RIGHT);
                                    TextFlow textFlow = (TextFlow) box.getChildren().get(0);
                                    textFlow.getStyleClass().remove("in_message");
                                    textFlow.getStyleClass().add("out_message");
                                }
                                messages.getChildren().add(box);
                            }
                        });

                        for(HBox user_box: users.getItems()){
                            TextFlow nameTextFlow = (TextFlow) user_box.getChildren().get(0);
                            Text nameText = (Text) nameTextFlow.getChildren().get(0);
                            if(nameText.getText().equals(user_to_send)){
                                TextFlow circle = (TextFlow) user_box.getChildren().get(1);
                                if(!circle.getStyleClass().isEmpty()){
                                    circle.getStyleClass().clear();
                                }
                                break;
                            }
                        }

                    }

                }
            });
            client.launch();
        }
    }
    @FXML
    public void sendMessageButtonClick() {
        sendMessage();
    }
    @FXML
    public void sendMessageEnterButton(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            sendMessage();
        }
    }
    private void sendMessage(){
        String out_text = message.getText().strip();
        if(!out_text.isEmpty()){
            chats.get(user_to_send).add(new Pair<>("out", out_text));
            client.sendMessage("message", out_text, user_to_send);
            HBox message_box = generateMessageBox(out_text);
            message_box.setAlignment(Pos.CENTER_RIGHT);
            TextFlow textFlow = (TextFlow) message_box.getChildren().get(0);
            textFlow.getStyleClass().remove("in_message");
            textFlow.getStyleClass().add("out_message");
            Platform.runLater(() -> {
                messages.getChildren().add(message_box);
            });
            message.clear();
        }
    }
    public void getMessage(String from, String message){
        chats.get(from).add(new Pair<>("in", message));
        if(user_to_send.equals(from)) {
            HBox message_box = generateMessageBox(message);
            Platform.runLater(() -> {
                messages.getChildren().add(message_box);
            });
        }
        else{
            for(HBox user_box: users.getItems()){
                TextFlow nameFlow = (TextFlow) user_box.getChildren().get(0);
                Text name = (Text) nameFlow.getChildren().get(0);
                if(name.getText().equals(from)){
                    TextFlow circle = (TextFlow) user_box.getChildren().get(1);
                    circle.getStyleClass().add("circle");
                    break;
                }
            }
        }
    }
    public void updateUsersList(ArrayList<String> usersList) {
        Platform.runLater(() -> {
            users.getItems().clear();
            for(String user: usersList){
                if(my_username.equals(user)) continue;
                users.getItems().add(generateUserBox(user));
                chats.put(user, new ArrayList<>());
            }
        });
    }
    public void addUserToUsersList(String name){
        Platform.runLater(() -> {
            users.getItems().add(generateUserBox(name));
            chats.put(name, new ArrayList<>());
        });
    }
    public void deleteUserFromUsersList(int idx, String username){
        if(user_to_send.equals(username)){
            chats.remove(username);
            user_to_send = "";
            chat.setDisable(true);
        }

        Platform.runLater(() -> {
            messages.getChildren().clear();
            users.getItems().remove(idx);
        });

    }
    public EventHandler<WindowEvent> getCloseEventHandler(){
        return (closeEventHandler) -> {
            if(client != null) client.close();
        };
    }
    private HBox generateUserBox(String name){
        HBox user_box = new HBox();
        TextFlow textFlow = new TextFlow(new Text(name));
        user_box.getChildren().add(textFlow);
        textFlow.setMinWidth(55);
        textFlow.setMaxWidth(55);
        TextFlow circle = new TextFlow();
        circle.setMaxWidth(10);
        circle.setMinWidth(10);
        user_box.getChildren().add(circle);
        return user_box;
    }
    private HBox generateMessageBox(String message){
        HBox message_box = new HBox();
        message_box.setPrefWidth(Double.MAX_VALUE);
        message_box.setAlignment(Pos.CENTER_LEFT);
        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPrefWidth(150);
        textFlow.getStyleClass().add("in_message");
        textFlow.getStyleClass().add("message");
        message_box.getChildren().add(textFlow);
        return message_box;
    }
}